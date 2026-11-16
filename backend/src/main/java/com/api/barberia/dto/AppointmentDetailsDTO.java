package com.api.barberia.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AppointmentDetailsDTO {
    
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    private BigDecimal price;

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long appointmentId;

    @NotNull(message = "El ID del servicio es obligatorio")
    private ServiceResponseDTO serviceId;
    
    // Getter y Setter
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public ServiceResponseDTO getServiceId() {
        return serviceId;
    }

    public void setServiceId(ServiceResponseDTO serviceId) {
        this.serviceId = serviceId;
    }
}
