package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.AppointmentDetailRequest;
import com.elcaballero.barberia.dto.ApiDtos.AppointmentDetailResponse;
import com.elcaballero.barberia.entity.Appointment;
import com.elcaballero.barberia.entity.AppointmentDetail;
import com.elcaballero.barberia.entity.Barber;
import com.elcaballero.barberia.entity.ServiceEntity;
import com.elcaballero.barberia.exception.BadRequestException;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.exception.ResourceNotFoundException;
import com.elcaballero.barberia.repository.AppointmentDetailRepository;
import com.elcaballero.barberia.repository.AppointmentRepository;
import com.elcaballero.barberia.repository.BarberRepository;
import com.elcaballero.barberia.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AppointmentDetailService {

    private final AppointmentDetailRepository details;
    private final AppointmentRepository appointments;
    private final BarberRepository barbers;
    private final ServiceRepository services;
    private final AppointmentService appointmentService;

    public AppointmentDetailService(
            AppointmentDetailRepository details,
            AppointmentRepository appointments,
            BarberRepository barbers,
            ServiceRepository services,
            AppointmentService appointmentService
    ) {
        this.details = details;
        this.appointments = appointments;
        this.barbers = barbers;
        this.services = services;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDetailResponse> findAll() {
        return details.findAll().stream().map(AppointmentService::toDetailResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentDetailResponse findById(Long id) {
        return AppointmentService.toDetailResponse(EntityLookup.detail(details, id));
    }

    public AppointmentDetailResponse create(AppointmentDetailRequest request) {
        Appointment appointment = lockAppointment(request.appointmentId());
        ServiceEntity service = activeService(request.serviceId());
        ensureServiceNotAlreadyAdded(appointment, service.getId(), null);
        AppointmentDetail detail = appointmentService.createDetail(appointment, service);
        appointments.save(appointment);
        return AppointmentService.toDetailResponse(detail);
    }

    public AppointmentDetailResponse update(Long id, AppointmentDetailRequest request) {
        AppointmentDetail detail = EntityLookup.detail(details, id);
        Appointment previousAppointment = detail.getAppointment();
        Appointment targetAppointment = request.appointmentId().equals(previousAppointment.getId())
                ? previousAppointment
                : lockAppointment(request.appointmentId());

        if (targetAppointment == previousAppointment) {
            barbers.findByIdForUpdate(targetAppointment.getBarber().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Barbero", targetAppointment.getBarber().getId()));
        }

        ServiceEntity service = activeService(request.serviceId());
        ensureServiceNotAlreadyAdded(targetAppointment, service.getId(),
                targetAppointment == previousAppointment ? detail.getId() : null);

        detail.setService(service);
        detail.setPrice(service.getPrice());
        if (targetAppointment != previousAppointment) {
            previousAppointment.removeDetail(detail);
            targetAppointment.addDetail(detail);
            appointmentService.recalculateDetails(previousAppointment);
            appointmentService.recalculateDetails(targetAppointment);
        } else {
            appointmentService.recalculateDetails(targetAppointment);
        }
        appointments.save(previousAppointment);
        if (targetAppointment != previousAppointment) {
            appointments.save(targetAppointment);
        }
        return AppointmentService.toDetailResponse(detail);
    }

    public void delete(Long id) {
        AppointmentDetail detail = EntityLookup.detail(details, id);
        Appointment appointment = detail.getAppointment();
        barbers.findByIdForUpdate(appointment.getBarber().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barbero", appointment.getBarber().getId()));
        appointment.removeDetail(detail);
        appointmentService.recalculateDetails(appointment);
        appointments.save(appointment);
    }

    private Appointment lockAppointment(Long id) {
        Appointment appointment = EntityLookup.appointment(appointments, id);
        Barber barber = EntityLookup.barberForUpdate(barbers, appointment.getBarber().getId());
        appointment.setBarber(barber);
        return appointment;
    }

    private ServiceEntity activeService(Long id) {
        ServiceEntity service = EntityLookup.service(services, id);
        if (!service.isActive()) {
            throw new BadRequestException("No se pueden agregar servicios inactivos a una cita.");
        }
        return service;
    }

    private static void ensureServiceNotAlreadyAdded(Appointment appointment, Long serviceId, Long excludedDetailId) {
        boolean alreadyAdded = appointment.getDetails().stream()
                .anyMatch(detail -> !Objects.equals(detail.getId(), excludedDetailId)
                        && detail.getService().getId().equals(serviceId));
        if (alreadyAdded) {
            throw new ConflictException("Ese servicio ya está agregado a la cita.");
        }
    }
}
