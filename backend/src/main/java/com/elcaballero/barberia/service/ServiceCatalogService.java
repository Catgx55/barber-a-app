package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.ServiceRequest;
import com.elcaballero.barberia.dto.ApiDtos.ServiceResponse;
import com.elcaballero.barberia.entity.ServiceEntity;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServiceCatalogService {

    private final ServiceRepository services;

    public ServiceCatalogService(ServiceRepository services) {
        this.services = services;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> findAll() {
        return services.findAll().stream().map(ServiceCatalogService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        return toResponse(EntityLookup.service(services, id));
    }

    public ServiceResponse create(ServiceRequest request) {
        String name = normalize(request.name());
        if (services.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un servicio con ese nombre.");
        }
        ServiceEntity service = new ServiceEntity();
        apply(service, request);
        return toResponse(services.save(service));
    }

    public ServiceResponse update(Long id, ServiceRequest request) {
        ServiceEntity service = EntityLookup.service(services, id);
        String name = normalize(request.name());
        if (!service.getName().equalsIgnoreCase(name) && services.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un servicio con ese nombre.");
        }
        apply(service, request);
        return toResponse(services.save(service));
    }

    // Los servicios vinculados a citas se desactivan para preservar el historial.
    public void delete(Long id) {
        ServiceEntity service = EntityLookup.service(services, id);
        service.setActive(false);
        services.save(service);
    }

    private static void apply(ServiceEntity service, ServiceRequest request) {
        service.setName(normalize(request.name()));
        service.setDescription(request.description().trim());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setActive(request.active());
    }

    private static String normalize(String name) {
        return name.trim();
    }

    private static ServiceResponse toResponse(ServiceEntity service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.isActive(),
                service.getCreatedAt()
        );
    }
}
