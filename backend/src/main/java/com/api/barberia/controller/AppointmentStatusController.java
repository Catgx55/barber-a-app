package com.api.barberia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.barberia.dto.AppointmentStatusDTO;
import com.api.barberia.repository.AppointmentStatusRepository;

/**
 * Supplies the appointment states used to render the staff dashboard.
 */
@RestController
@RequestMapping("/api/appointment-statuses")
public class AppointmentStatusController {

    private final AppointmentStatusRepository statuses;

    public AppointmentStatusController(AppointmentStatusRepository statuses) {
        this.statuses = statuses;
    }

    @GetMapping
    public List<AppointmentStatusDTO> findAll() {
        return statuses.findAll().stream().map(status -> {
            AppointmentStatusDTO response = new AppointmentStatusDTO();
            response.setId(status.getId());
            response.setName(status.getName());
            return response;
        }).toList();
    }
}
