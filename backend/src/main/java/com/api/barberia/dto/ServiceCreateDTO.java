package com.api.barberia.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ServiceCreateDTO {
    
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 100)
    private String name;
    
    @NotBlank(message = "La especificación del servicio es obligatorio")
    @Size(max = 500)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un número positivo")
    private BigDecimal price;
    
    @NotNull(message = "El tiempo de atención es obligatorio")
    @Positive(message = "El tiempo no puede ser negativo")
    private Integer durationMinutes;

    @NotNull(message = "Debes especificar si el servicio esta activo o no")
    private Boolean isActive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
