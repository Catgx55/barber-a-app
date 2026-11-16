package com.api.barberia.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * The barber dashboard is allowed to change only the status of an appointment.
 */
public record AppointmentStatusUpdateDTO(@NotNull @Positive Long statusId) {
}
