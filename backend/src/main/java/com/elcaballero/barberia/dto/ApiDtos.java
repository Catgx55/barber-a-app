package com.elcaballero.barberia.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// Las respuestas explícitas impiden exponer las contraseñas y las relaciones internas de Hibernate.
public final class ApiDtos {

    private ApiDtos() {
    }

    public record RoleRequest(@NotBlank @Size(max = 60) String name) {
    }

    public record RoleResponse(Long id, String name) {
    }

    public record ServiceRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 500) String description,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
            @NotNull @Positive Integer durationMinutes,
            @NotNull Boolean active
    ) {
    }

    public record ServiceResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            boolean active,
            LocalDateTime createdAt
    ) {
    }

    public record AppointmentStatusRequest(@NotBlank @Size(max = 60) String name) {
    }

    public record AppointmentStatusResponse(Long id, String name) {
    }

    public record UserRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,25}$") String phone,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotNull @Positive Long roleId
    ) {
    }

    public record UserResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            Long roleId,
            String roleName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record BarberRequest(
            @NotNull @Positive Long userId,
            @NotBlank @Size(max = 150) String specialty,
            @NotNull Boolean active
    ) {
    }

    public record BarberResponse(
            Long id,
            Long userId,
            String firstName,
            String lastName,
            String email,
            String specialty,
            boolean active,
            LocalDateTime createdAt
    ) {
    }

    public record AppointmentRequest(
            @NotNull @Positive Long clientId,
            @NotNull @Positive Long barberId,
            @NotNull @Positive Long statusId,
            @NotNull LocalDate appointmentDate,
            @NotNull LocalTime startTime,
            @Size(max = 1000) String notes,
            @NotEmpty List<@NotNull @Positive Long> serviceIds
    ) {
    }

    public record AppointmentDetailResponse(
            Long id,
            Long serviceId,
            String serviceName,
            BigDecimal price
    ) {
    }

    public record AppointmentResponse(
            Long id,
            Long clientId,
            String clientName,
            Long barberId,
            String barberName,
            Long statusId,
            String statusName,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal totalAmount,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<AppointmentDetailResponse> details
    ) {
    }

    public record AppointmentDetailRequest(
            @NotNull @Positive Long appointmentId,
            @NotNull @Positive Long serviceId
    ) {
    }
}
