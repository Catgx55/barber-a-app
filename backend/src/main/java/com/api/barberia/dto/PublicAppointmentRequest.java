package com.api.barberia.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Public booking input: the server derives the price, duration and initial
 * status instead of trusting values supplied by the browser.
 */
public record PublicAppointmentRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,20}$") String phone,
        @NotNull @Positive Long barberId,
        @NotNull LocalDate appointmentDate,
        @NotNull LocalTime startTime,
        @Size(max = 1000) String notes,
        @NotEmpty @Size(max = 8) List<@NotNull @Positive Long> serviceIds,
        @NotBlank @Pattern(regexp = "NEQUI|DAVIPLATA|EFECTIVO") String paymentMethod) {
}
