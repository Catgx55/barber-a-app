package com.elcaballero.barberia.controller;

import com.elcaballero.barberia.dto.ApiDtos.AppointmentStatusRequest;
import com.elcaballero.barberia.dto.ApiDtos.AppointmentStatusResponse;
import com.elcaballero.barberia.service.AppointmentStatusService;
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
@RequestMapping("/api/appointment-statuses")
public class AppointmentStatusController {

    private final AppointmentStatusService statuses;

    public AppointmentStatusController(AppointmentStatusService statuses) {
        this.statuses = statuses;
    }

    @GetMapping
    public List<AppointmentStatusResponse> findAll() {
        return statuses.findAll();
    }

    @GetMapping("/{id}")
    public AppointmentStatusResponse findById(@PathVariable Long id) {
        return statuses.findById(id);
    }

    @PostMapping
    public ResponseEntity<AppointmentStatusResponse> create(@Valid @RequestBody AppointmentStatusRequest request) {
        AppointmentStatusResponse created = statuses.create(request);
        return ResponseEntity.created(URI.create("/api/appointment-statuses/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AppointmentStatusResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {
        return statuses.update(id, request);
    }

    @PatchMapping("/{id}")
    public AppointmentStatusResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {
        return statuses.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statuses.delete(id);
        return ResponseEntity.noContent().build();
    }
}
