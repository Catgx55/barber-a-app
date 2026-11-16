package com.api.barberia.controller;

import java.util.List;
import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.barberia.dto.BarberResponseDTO;
import com.api.barberia.dto.PublicAppointmentRequest;
import com.api.barberia.dto.AppointmentResponseDTO;
import com.api.barberia.service.BarberService;
import com.api.barberia.service.PublicBookingService;

import jakarta.validation.Valid;

/**
 * Exposes only the public catalog needed for booking, never staff or client data.
 */
@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    private final BarberService barbers;
    private final PublicBookingService bookings;

    public PublicBookingController(BarberService barbers, PublicBookingService bookings) {
        this.barbers = barbers;
        this.bookings = bookings;
    }

    @GetMapping("/barbers")
    public List<BarberResponseDTO> findActiveBarbers() {
        return barbers.getActiveBarbers();
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody PublicAppointmentRequest request) {
        AppointmentResponseDTO created = bookings.create(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + created.getId())).body(created);
    }
}
