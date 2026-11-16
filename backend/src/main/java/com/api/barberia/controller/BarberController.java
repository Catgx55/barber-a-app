package com.api.barberia.controller;

import com.api.barberia.dto.BarberCreateDTO;
import com.api.barberia.dto.BarberResponseDTO;
import com.api.barberia.service.BarberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {

    
    private final BarberService barberService;

    public BarberController(BarberService barberService) {
        this.barberService = barberService;
    }

    @GetMapping
    public ResponseEntity<List<BarberResponseDTO>> getAllBarbers() {
        return ResponseEntity.ok(barberService.getAllBarbers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<BarberResponseDTO>> getActiveBarbers() {
        return ResponseEntity.ok(barberService.getActiveBarbers());
    }

    @PostMapping
    public ResponseEntity<BarberResponseDTO> createBarber(@Valid @RequestBody BarberCreateDTO createDTO) {
        BarberResponseDTO newBarber = barberService.createBarber(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBarber);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberResponseDTO> updateBarber(
            @PathVariable Long id, 
            @Valid @RequestBody BarberCreateDTO createDTO) {
        BarberResponseDTO updatedBarber = barberService.updateBarber(id, createDTO);
        return ResponseEntity.ok(updatedBarber);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBarber(@PathVariable Long id) {
        barberService.deleteBarber(id);
        return ResponseEntity.noContent().build();
    }
}
