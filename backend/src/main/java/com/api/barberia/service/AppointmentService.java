package com.api.barberia.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.api.barberia.dto.AppointmentCreateDTO;
import com.api.barberia.dto.AppointmentResponseDTO;
import com.api.barberia.dto.AppointmentServiceItemDTO;
import com.api.barberia.dto.AppointmentStatusDTO;
import com.api.barberia.dto.AppointmentStatusUpdateDTO;
import com.api.barberia.dto.BarberResponseDTO;
import com.api.barberia.dto.UserResponseDTO;
import com.api.barberia.model.AppointmentDetails;
import com.api.barberia.model.AppointmentStatuses;
import com.api.barberia.model.Appointments;
import com.api.barberia.model.Barbers;
import com.api.barberia.model.Services;
import com.api.barberia.model.Users;
import com.api.barberia.repository.AppointmentDetailRepository;
import com.api.barberia.repository.AppointmentRepository;
import com.api.barberia.repository.AppointmentStatusRepository;
import com.api.barberia.repository.BarberRepository;
import com.api.barberia.repository.ServiceRepository;
import com.api.barberia.repository.UserRepository;

/**
 * Coordinates appointment persistence, service snapshots, and barber ownership
 * checks. Controllers keep only HTTP-specific behavior.
 */
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentDetailRepository appointmentDetailRepository;
    private final UserRepository userRepository;
    private final BarberRepository barberRepository;
    private final AppointmentStatusRepository statusRepository;
    private final ServiceRepository serviceRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentDetailRepository appointmentDetailRepository,
            UserRepository userRepository,
            BarberRepository barberRepository,
            AppointmentStatusRepository statusRepository,
            ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentDetailRepository = appointmentDetailRepository;
        this.userRepository = userRepository;
        this.barberRepository = barberRepository;
        this.statusRepository = statusRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsForBarber(String email) {
        Barbers barber = findBarberForUser(email);
        return appointmentRepository.findByBarberIdId(barber.getId()).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return mapToResponseDTO(findAppointment(id));
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentForBarber(Long id, String email) {
        Appointments appointment = findAppointment(id);
        ensureOwnedBy(appointment, email);
        return mapToResponseDTO(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByDateForBarber(LocalDate date, String email) {
        Barbers barber = findBarberForUser(email);
        return appointmentRepository.findByBarberIdIdAndAppointmentDate(barber.getId(), date).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Creates the appointment and service-price snapshots in one transaction.
     */
    public AppointmentResponseDTO createAppointment(AppointmentCreateDTO request, List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona al menos un servicio.");
        }

        Users client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        Barbers barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barbero no encontrado."));
        AppointmentStatuses status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado."));

        Appointments appointment = new Appointments();
        appointment.setClientId(client);
        appointment.setBarberId(barber);
        appointment.setStatusId(status);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setTotalAmount(request.getTotalAmount());
        appointment.setNotes(request.getNotes());

        Appointments saved = appointmentRepository.save(appointment);
        saveServiceDetails(saved, serviceIds);
        return mapToResponseDTO(saved);
    }

    /**
     * Updates appointment information and replaces its service snapshots atomically.
     */
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentCreateDTO request, List<Long> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona al menos un servicio.");
        }

        Appointments appointment = findAppointment(id);
        Users client = userRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        Barbers barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barbero no encontrado."));
        AppointmentStatuses status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado."));

        appointment.setClientId(client);
        appointment.setBarberId(barber);
        appointment.setStatusId(status);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setTotalAmount(request.getTotalAmount());
        appointment.setNotes(request.getNotes());

        appointmentRepository.save(appointment);
        appointmentDetailRepository.deleteByAppointmentIdId(id);
        saveServiceDetails(appointment, serviceIds);
        return mapToResponseDTO(appointment);
    }

    public AppointmentResponseDTO updateStatus(
            Long appointmentId,
            AppointmentStatusUpdateDTO request,
            String email,
            boolean administrator) {
        Appointments appointment = findAppointment(appointmentId);
        if (!administrator) {
            ensureOwnedBy(appointment, email);
        }
        AppointmentStatuses status = statusRepository.findById(request.statusId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estado no encontrado."));
        appointment.setStatusId(status);
        return mapToResponseDTO(appointmentRepository.save(appointment));
    }

    public void deleteAppointment(Long id) {
        Appointments appointment = findAppointment(id);
        appointmentDetailRepository.deleteByAppointmentIdId(id);
        appointmentRepository.delete(appointment);
    }

    private void saveServiceDetails(Appointments appointment, List<Long> serviceIds) {
        for (Long serviceId : serviceIds.stream().distinct().toList()) {
            Services service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Servicio no encontrado: " + serviceId));
            AppointmentDetails detail = new AppointmentDetails();
            detail.setAppointmentId(appointment);
            detail.setServiceId(service);
            detail.setPrice(service.getPrice());
            appointmentDetailRepository.save(detail);
        }
    }

    private Users findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    private Barbers findBarberForUser(String email) {
        Users user = findUserByEmail(email);
        return barberRepository.findByUserIdId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "La cuenta no tiene un perfil de barbero."));
    }

    private Appointments findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cita no encontrada con ID: " + id));
    }

    private static void ensureOwnedBy(Appointments appointment, String email) {
        if (appointment.getBarberId() == null
                || appointment.getBarberId().getUser() == null
                || !appointment.getBarberId().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes consultar ni modificar citas de otro barbero.");
        }
    }

    private AppointmentResponseDTO mapToResponseDTO(Appointments appointment) {
        AppointmentResponseDTO response = new AppointmentResponseDTO();
        response.setId(appointment.getId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setTotalAmount(appointment.getTotalAmount());
        response.setNotes(appointment.getNotes());
        response.setPaymentMethod(appointment.getPaymentMethod());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());

        Users client = appointment.getClientId();
        if (client != null) {
            UserResponseDTO clientResponse = new UserResponseDTO();
            clientResponse.setId(client.getId());
            clientResponse.setFirstName(client.getFirstName());
            clientResponse.setLastName(client.getLastName());
            clientResponse.setEmail(client.getEmail());
            clientResponse.setPhone(client.getPhone());
            response.setClient(clientResponse);
            response.setClientName(client.getFirstName() + " " + client.getLastName());
            response.setClientPhone(client.getPhone());
        }

        Barbers barber = appointment.getBarberId();
        if (barber != null) {
            BarberResponseDTO barberResponse = new BarberResponseDTO();
            barberResponse.setId(barber.getId());
            barberResponse.setSpecialty(barber.getSpecialty());
            barberResponse.setIsActive(barber.isActive());
            barberResponse.setUserId(barber.getUser().getId());
            barberResponse.setFirstName(barber.getUser().getFirstName());
            barberResponse.setLastName(barber.getUser().getLastName());
            response.setBarber(barberResponse);
            response.setBarberName(barber.getUser().getFirstName() + " " + barber.getUser().getLastName());
        }

        AppointmentStatuses status = appointment.getStatusId();
        if (status != null) {
            AppointmentStatusDTO statusResponse = new AppointmentStatusDTO();
            statusResponse.setId(status.getId());
            statusResponse.setName(status.getName());
            response.setStatus(statusResponse);
            response.setStatusId(status.getId());
            response.setStatusName(status.getName());
        }

        List<AppointmentServiceItemDTO> details = appointmentDetailRepository
                .findByAppointmentIdId(appointment.getId()).stream()
                .map(detail -> new AppointmentServiceItemDTO(
                        detail.getServiceId().getName(), detail.getPrice()))
                .toList();
        response.setDetails(details);
        return response;
    }
}
