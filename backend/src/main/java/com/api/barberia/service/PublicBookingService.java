package com.api.barberia.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.barberia.dto.AppointmentResponseDTO;
import com.api.barberia.dto.PublicAppointmentRequest;
import com.api.barberia.model.AppointmentDetails;
import com.api.barberia.model.AppointmentStatuses;
import com.api.barberia.model.Appointments;
import com.api.barberia.model.Barbers;
import com.api.barberia.model.Roles;
import com.api.barberia.model.Services;
import com.api.barberia.model.Users;
import com.api.barberia.repository.AppointmentDetailRepository;
import com.api.barberia.repository.AppointmentRepository;
import com.api.barberia.repository.AppointmentStatusRepository;
import com.api.barberia.repository.BarberRepository;
import com.api.barberia.repository.RoleRepository;
import com.api.barberia.repository.ServiceRepository;
import com.api.barberia.repository.UserRepository;

/**
 * Validates public booking input and calculates all prices and times on the
 * server before persisting a pending appointment.
 */
@Service
public class PublicBookingService {

    private final AppointmentRepository appointments;
    private final AppointmentDetailRepository details;
    private final AppointmentStatusRepository statuses;
    private final BarberRepository barbers;
    private final RoleRepository roles;
    private final ServiceRepository services;
    private final UserRepository users;
    private final AppointmentService appointmentService;
    private final PasswordEncoder passwordEncoder;

    public PublicBookingService(
            AppointmentRepository appointments,
            AppointmentDetailRepository details,
            AppointmentStatusRepository statuses,
            BarberRepository barbers,
            RoleRepository roles,
            ServiceRepository services,
            UserRepository users,
            AppointmentService appointmentService,
            PasswordEncoder passwordEncoder) {
        this.appointments = appointments;
        this.details = details;
        this.statuses = statuses;
        this.barbers = barbers;
        this.roles = roles;
        this.services = services;
        this.users = users;
        this.appointmentService = appointmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppointmentResponseDTO create(PublicAppointmentRequest request) {
        if (new HashSet<>(request.serviceIds()).size() != request.serviceIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No repitas un servicio en la misma cita.");
        }

        Barbers barber = barbers.findByIdForUpdate(request.barberId())
                .filter(Barbers::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El barbero no existe o no está activo."));
        Users client = findOrCreateClient(request);
        AppointmentStatuses pending = statuses.findByNameIgnoreCase("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("Falta el estado inicial PENDIENTE."));

        BigDecimal total = BigDecimal.ZERO;
        int durationMinutes = 0;
        var selectedServices = request.serviceIds().stream()
                .map(id -> services.findById(id)
                        .filter(Services::getIsActive)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El servicio " + id + " no existe o no está activo.")))
                .toList();

        for (Services service : selectedServices) {
            total = total.add(service.getPrice());
            durationMinutes += service.getDurationMinutes();
        }

        LocalTime endTime = request.startTime().plusMinutes(durationMinutes);
        validateAppointmentTime(request.appointmentDate(), request.startTime(), endTime);
        validateAvailability(barber, request.appointmentDate(), request.startTime(), endTime);

        Appointments appointment = new Appointments();
        appointment.setClientId(client);
        appointment.setBarberId(barber);
        appointment.setStatusId(pending);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(endTime);
        appointment.setTotalAmount(total);
        appointment.setNotes(request.notes() == null ? "" : request.notes().trim());
        appointment.setPaymentMethod(request.paymentMethod());

        Appointments saved = appointments.save(appointment);
        for (Services service : selectedServices) {
            AppointmentDetails detail = new AppointmentDetails();
            detail.setAppointmentId(saved);
            detail.setServiceId(service);
            detail.setPrice(service.getPrice());
            details.save(detail);
        }

        return appointmentService.getAppointmentById(saved.getId());
    }

    private Users findOrCreateClient(PublicAppointmentRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        var existing = users.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            Users client = existing.get();
            if (!"CLIENT".equalsIgnoreCase(client.getRolesId().getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ese correo pertenece a una cuenta del personal. Usa otro correo para reservar.");
            }
            client.setFirstName(request.firstName().trim());
            client.setLastName(request.lastName().trim());
            client.setPhone(request.phone().trim());
            return users.save(client);
        }

        Roles clientRole = roles.findByNameIgnoreCase("CLIENT")
                .orElseThrow(() -> new IllegalStateException("Falta el rol CLIENT."));
        Users client = new Users();
        client.setFirstName(request.firstName().trim());
        client.setLastName(request.lastName().trim());
        client.setPhone(request.phone().trim());
        client.setEmail(email);
        // Public reservations do not create a usable login for customer accounts.
        client.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        client.setRolesId(clientRole);
        return users.save(client);
    }

    private static void validateAppointmentTime(
            java.time.LocalDate date,
            LocalTime start,
            LocalTime end) {
        LocalDateTime nowInBusinessZone = LocalDateTime.now(ZoneId.of("America/Bogota"));
        if (!LocalDateTime.of(date, start).isAfter(nowInBusinessZone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita debe ser en el futuro.");
        }

        DayOfWeek day = date.getDayOfWeek();
        LocalTime opening = day == DayOfWeek.SUNDAY ? LocalTime.of(10, 0) : LocalTime.of(8, 0);
        LocalTime closing = switch (day) {
            case SATURDAY -> LocalTime.of(18, 0);
            case SUNDAY -> LocalTime.of(16, 0);
            default -> LocalTime.of(20, 0);
        };
        if (start.isBefore(opening) || end.isAfter(closing)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El horario solicitado está fuera del horario de atención.");
        }
    }

    private void validateAvailability(Barbers barber, java.time.LocalDate date, LocalTime start, LocalTime end) {
        boolean overlaps = appointments.findByBarberIdIdAndAppointmentDate(barber.getId(), date).stream()
                .filter(existing -> !"CANCELADA".equalsIgnoreCase(existing.getStatusId().getName()))
                .anyMatch(existing -> start.isBefore(existing.getEndTime()) && end.isAfter(existing.getStartTime()));
        if (overlaps) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ese horario ya está ocupado. Selecciona otra hora.");
        }
    }
}
