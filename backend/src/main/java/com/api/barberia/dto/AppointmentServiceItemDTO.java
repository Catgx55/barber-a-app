package com.api.barberia.dto;

import java.math.BigDecimal;

/**
 * Small service snapshot used by the barber dashboard for each appointment.
 */
public record AppointmentServiceItemDTO(String serviceName, BigDecimal price) {
}
