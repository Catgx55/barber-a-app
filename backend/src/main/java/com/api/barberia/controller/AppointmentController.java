package com.api.barberia.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.barberia.dto.AppointmentCreateDTO;
import com.api.barberia.dto.AppointmentResponseDTO;
import com.api.barberia.dto.AppointmentStatusUpdateDTO;
import com.api.barberia.service.AppointmentService;

import jakarta.validation.Valid;

/**
 * Admins manage the full appointment record; barbers can only view their own
 * schedule and update appointment status.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponseDTO> getAppointments(Authentication authentication) {
        return isAdministrator(authentication)
                ? appointmentService.getAllAppointments()
                : appointmentService.getAppointmentsForBarber(authentication.getName());
    }

    @GetMapping("/{id}")
    public AppointmentResponseDTO getAppointment(@PathVariable Long id, Authentication authentication) {
        return isAdministrator(authentication)
                ? appointmentService.getAppointmentById(id)
                : appointmentService.getAppointmentForBarber(id, authentication.getName());
    }

    @GetMapping("/date/{date}")
    public List<AppointmentResponseDTO> getAppointmentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        return isAdministrator(authentication)
                ? appointmentService.getAppointmentsByDate(date)
                : appointmentService.getAppointmentsByDateForBarber(date, authentication.getName());
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentCreateDTO request,
            @RequestParam List<Long> serviceIds) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(request, serviceIds));
    }

    @PutMapping("/{id}")
    public AppointmentResponseDTO updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentCreateDTO request,
            @RequestParam List<Long> serviceIds) {
        return appointmentService.updateAppointment(id, request, serviceIds);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponseDTO updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateDTO request,
            Authentication authentication) {
        return appointmentService.updateStatus(id, request, authentication.getName(),
                isAdministrator(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    private static boolean isAdministrator(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
