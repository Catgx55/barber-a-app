package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.AppointmentDetailResponse;
import com.elcaballero.barberia.dto.ApiDtos.AppointmentRequest;
import com.elcaballero.barberia.dto.ApiDtos.AppointmentResponse;
import com.elcaballero.barberia.entity.Appointment;
import com.elcaballero.barberia.entity.AppointmentDetail;
import com.elcaballero.barberia.entity.AppointmentStatus;
import com.elcaballero.barberia.entity.Barber;
import com.elcaballero.barberia.entity.ServiceEntity;
import com.elcaballero.barberia.entity.User;
import com.elcaballero.barberia.exception.BadRequestException;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.repository.AppointmentRepository;
import com.elcaballero.barberia.repository.AppointmentStatusRepository;
import com.elcaballero.barberia.repository.BarberRepository;
import com.elcaballero.barberia.repository.ServiceRepository;
import com.elcaballero.barberia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final BarberRepository barbers;
    private final AppointmentStatusRepository statuses;
    private final ServiceRepository services;

    public AppointmentService(
            AppointmentRepository appointments,
            UserRepository users,
            BarberRepository barbers,
            AppointmentStatusRepository statuses,
            ServiceRepository services
    ) {
        this.appointments = appointments;
        this.users = users;
        this.barbers = barbers;
        this.statuses = statuses;
        this.services = services;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointments.findAll().stream().map(AppointmentService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return toResponse(EntityLookup.appointment(appointments, id));
    }

    public AppointmentResponse create(AppointmentRequest request) {
        validateAppointmentTime(request.appointmentDate(), request.startTime());
        Barber barber = EntityLookup.barberForUpdate(barbers, request.barberId());
        if (!barber.isActive()) {
            throw new BadRequestException("No se pueden agendar citas con un barbero inactivo.");
        }

        User client = EntityLookup.user(users, request.clientId());
        AppointmentStatus status = EntityLookup.status(statuses, request.statusId());
        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setStatus(status);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setNotes(normalizeNotes(request.notes()));
        updateDetails(appointment, request.serviceIds());

        assertAvailable(appointment, null);
        return toResponse(appointments.save(appointment));
    }

    public AppointmentResponse update(Long id, AppointmentRequest request) {
        validateAppointmentTime(request.appointmentDate(), request.startTime());
        Appointment appointment = EntityLookup.appointment(appointments, id);
        Barber barber = EntityLookup.barberForUpdate(barbers, request.barberId());
        if (!barber.isActive()) {
            throw new BadRequestException("No se pueden agendar citas con un barbero inactivo.");
        }

        appointment.setClient(EntityLookup.user(users, request.clientId()));
        appointment.setBarber(barber);
        appointment.setStatus(EntityLookup.status(statuses, request.statusId()));
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setNotes(normalizeNotes(request.notes()));
        updateDetails(appointment, request.serviceIds());

        assertAvailable(appointment, id);
        return toResponse(appointments.save(appointment));
    }

    public void delete(Long id) {
        appointments.delete(EntityLookup.appointment(appointments, id));
    }

    void recalculateDetails(Appointment appointment) {
        BigDecimal total = BigDecimal.ZERO;
        long duration = 0;
        for (AppointmentDetail detail : appointment.getDetails()) {
            total = total.add(detail.getPrice());
            duration += detail.getService().getDurationMinutes();
        }

        if (duration >= 24 * 60) {
            throw new BadRequestException("La duración total de los servicios no puede exceder un día.");
        }

        LocalTime endTime = appointment.getStartTime().plusMinutes(duration);
        if (!endTime.equals(appointment.getEndTime())) {
            appointment.setEndTime(endTime);
        }
        appointment.setTotalAmount(total);
        assertAvailable(appointment, appointment.getId());
    }

    AppointmentDetail createDetail(Appointment appointment, ServiceEntity service) {
        AppointmentDetail detail = new AppointmentDetail();
        detail.setService(service);
        detail.setPrice(service.getPrice());
        appointment.addDetail(detail);
        recalculateDetails(appointment);
        return detail;
    }

    static AppointmentDetailResponse toDetailResponse(AppointmentDetail detail) {
        return new AppointmentDetailResponse(
                detail.getId(),
                detail.getService().getId(),
                detail.getService().getName(),
                detail.getPrice()
        );
    }

    private void updateDetails(Appointment appointment, List<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            throw new BadRequestException("La cita debe incluir al menos un servicio.");
        }
        if (new HashSet<>(serviceIds).size() != serviceIds.size()) {
            throw new BadRequestException("No se puede repetir un servicio dentro de la misma cita.");
        }

        appointment.getDetails().clear();
        for (Long serviceId : serviceIds) {
            ServiceEntity service = EntityLookup.service(services, serviceId);
            if (!service.isActive()) {
                throw new BadRequestException("El servicio " + service.getName() + " está inactivo.");
            }

            AppointmentDetail detail = new AppointmentDetail();
            detail.setService(service);
            detail.setPrice(service.getPrice());
            appointment.addDetail(detail);
        }
        recalculateDetails(appointment);
    }

    private void assertAvailable(Appointment appointment, Long excludeId) {
        if (!appointment.getEndTime().isAfter(appointment.getStartTime())) {
            throw new BadRequestException("La cita debe finalizar el mismo día en que empieza.");
        }
        if (appointments.existsOverlappingAppointment(
                appointment.getBarber().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                excludeId
        )) {
            throw new ConflictException("El barbero ya tiene una cita en ese horario.");
        }
    }

    private static void validateAppointmentTime(LocalDate date, LocalTime time) {
        if (!LocalDateTime.of(date, time).isAfter(LocalDateTime.now(ZoneId.of("America/Bogota")))) {
            throw new BadRequestException("La fecha y hora de la cita deben ser futuras.");
        }
    }

    private static String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }

    static AppointmentResponse toResponse(Appointment appointment) {
        List<AppointmentDetailResponse> details = appointment.getDetails().stream()
                .map(AppointmentService::toDetailResponse)
                .toList();
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getClient().getId(),
                appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName(),
                appointment.getBarber().getId(),
                appointment.getBarber().getUser().getFirstName() + " "
                        + appointment.getBarber().getUser().getLastName(),
                appointment.getStatus().getId(),
                appointment.getStatus().getName(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getTotalAmount(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                details
        );
    }
}
