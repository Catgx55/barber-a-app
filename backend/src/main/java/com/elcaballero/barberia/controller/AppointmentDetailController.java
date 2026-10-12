package com.elcaballero.barberia.controller;

import com.elcaballero.barberia.dto.ApiDtos.AppointmentDetailRequest;
import com.elcaballero.barberia.dto.ApiDtos.AppointmentDetailResponse;
import com.elcaballero.barberia.service.AppointmentDetailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/appointment-details")
public class AppointmentDetailController {

    private final AppointmentDetailService details;

    public AppointmentDetailController(AppointmentDetailService details) {
        this.details = details;
    }

    @GetMapping
    public List<AppointmentDetailResponse> findAll() {
        return details.findAll();
    }

    @GetMapping("/{id}")
    public AppointmentDetailResponse findById(@PathVariable Long id) {
        return details.findById(id);
    }

    @PostMapping
    public ResponseEntity<AppointmentDetailResponse> create(@Valid @RequestBody AppointmentDetailRequest request) {
        AppointmentDetailResponse created = details.create(request);
        return ResponseEntity.created(URI.create("/api/appointment-details/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AppointmentDetailResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDetailRequest request
    ) {
        return details.update(id, request);
    }

    @PatchMapping("/{id}")
    public AppointmentDetailResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDetailRequest request
    ) {
        return details.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        details.delete(id);
        return ResponseEntity.noContent().build();
    }
}
