package com.elcaballero.barberia.controller;

import com.elcaballero.barberia.dto.ApiDtos.BarberRequest;
import com.elcaballero.barberia.dto.ApiDtos.BarberResponse;
import com.elcaballero.barberia.service.BarberService;
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
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barbers;

    public BarberController(BarberService barbers) {
        this.barbers = barbers;
    }

    @GetMapping
    public List<BarberResponse> findAll() {
        return barbers.findAll();
    }

    @GetMapping("/{id}")
    public BarberResponse findById(@PathVariable Long id) {
        return barbers.findById(id);
    }

    @PostMapping
    public ResponseEntity<BarberResponse> create(@Valid @RequestBody BarberRequest request) {
        BarberResponse created = barbers.create(request);
        return ResponseEntity.created(URI.create("/api/barbers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public BarberResponse update(@PathVariable Long id, @Valid @RequestBody BarberRequest request) {
        return barbers.update(id, request);
    }

    @PatchMapping("/{id}")
    public BarberResponse patch(@PathVariable Long id, @Valid @RequestBody BarberRequest request) {
        return barbers.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        barbers.delete(id);
        return ResponseEntity.noContent().build();
    }
}
