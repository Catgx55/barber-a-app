package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.AppointmentStatusRequest;
import com.elcaballero.barberia.dto.ApiDtos.AppointmentStatusResponse;
import com.elcaballero.barberia.entity.AppointmentStatus;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.repository.AppointmentStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppointmentStatusService {

    private final AppointmentStatusRepository statuses;

    public AppointmentStatusService(AppointmentStatusRepository statuses) {
        this.statuses = statuses;
    }

    @Transactional(readOnly = true)
    public List<AppointmentStatusResponse> findAll() {
        return statuses.findAll().stream().map(AppointmentStatusService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentStatusResponse findById(Long id) {
        return toResponse(EntityLookup.status(statuses, id));
    }

    public AppointmentStatusResponse create(AppointmentStatusRequest request) {
        String name = request.name().trim();
        if (statuses.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un estado con ese nombre.");
        }
        AppointmentStatus status = new AppointmentStatus();
        status.setName(name);
        return toResponse(statuses.save(status));
    }

    public AppointmentStatusResponse update(Long id, AppointmentStatusRequest request) {
        AppointmentStatus status = EntityLookup.status(statuses, id);
        String name = request.name().trim();
        if (!status.getName().equalsIgnoreCase(name) && statuses.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un estado con ese nombre.");
        }
        status.setName(name);
        return toResponse(statuses.save(status));
    }

    public void delete(Long id) {
        statuses.delete(EntityLookup.status(statuses, id));
    }

    private static AppointmentStatusResponse toResponse(AppointmentStatus status) {
        return new AppointmentStatusResponse(status.getId(), status.getName());
    }
}
