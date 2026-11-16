package com.api.barberia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BarberCreateDTO {
    
    @NotBlank(message = "La especialidad del barbero es obligatoria")
    @Size(max = 150)
    private String specialty;

    @NotNull(message = "El barbero esta activo o no")
    private Boolean isActive;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


}
