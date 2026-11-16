package com.api.barberia.controller;

import com.api.barberia.dto.ServiceCreateDTO;
import com.api.barberia.dto.ServiceResponseDTO;
import com.api.barberia.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    
    private final ServiceService serviceService;
    
    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> getAllServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ServiceResponseDTO>> getActiveServices() {
        return ResponseEntity.ok(serviceService.getActiveServices());
    }

    @PostMapping
    public ResponseEntity<ServiceResponseDTO> createService(@Valid @RequestBody ServiceCreateDTO createDTO) {
        ServiceResponseDTO newService = serviceService.createService(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newService);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> updateService(
            @PathVariable Long id, 
            @Valid @RequestBody ServiceCreateDTO createDTO) {
        ServiceResponseDTO updatedService = serviceService.updateService(id, createDTO);
        return ResponseEntity.ok(updatedService);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build(); // Retorna código 204 No Content al eliminar con éxito
    }
}
