package com.api.barberia.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.api.barberia.dto.ServiceCreateDTO;
import com.api.barberia.dto.ServiceResponseDTO;
import com.api.barberia.model.Services;
import com.api.barberia.repository.ServiceRepository;

@Service
public class ServiceService {
    
    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // Obtiene todos los servicios registrados.
    public List<ServiceResponseDTO> getAllServices() {
        return serviceRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // Obtiene únicamente los servicios activos.
    public List<ServiceResponseDTO> getActiveServices() {
        return serviceRepository.findByIsActiveTrue().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // Crea un nuevo servicio en el sistema.
    public ServiceResponseDTO createService(ServiceCreateDTO createDTO) {
        Services service = new Services();
        service.setName(createDTO.getName());
        service.setDescription(createDTO.getDescription());
        service.setPrice(createDTO.getPrice());
        service.setDurationMinutes(createDTO.getDurationMinutes());
        service.setIsActive(createDTO.getIsActive());

        Services saved = serviceRepository.save(service);
        return mapToResponseDTO(saved);
    }

    
    // Actualiza un servicio existente.
    public ServiceResponseDTO updateService(Long id, ServiceCreateDTO dto) {
        Services service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado."));
        
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice());
        service.setDurationMinutes(dto.getDurationMinutes());
        service.setIsActive(dto.getIsActive());
        
        Services updatedService = serviceRepository.save(service);
        return mapToResponseDTO(updatedService);
    }
    
    // Elimina un servicio por su ID.
    public void deleteService(Long id) {
        Services service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado."));
        // Keep historic appointment details intact; only hide the service from new bookings.
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    // Mapea una entidad Services a ServiceResponseDTO.
    private ServiceResponseDTO mapToResponseDTO(Services service) {
        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setIsActive(service.getIsActive());
        dto.setCreatedAt(service.getCreatedAt());
        return dto;
    }

}
