package com.api.barberia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.barberia.model.Appointments;

public interface AppointmentRepository extends JpaRepository<Appointments, Long> {
    
    // 1. Lista todas las citas de una fecha específica (Muy útil para la agenda del día)
    List<Appointments> findByAppointmentDate(LocalDate appointmentDate);

    // 2. Busca el historial de citas de un cliente usando su ID numérico
    List<Appointments> findByClientIdId(Long clientId);

    // 3. Busca la agenda de citas de un barbero específico por su ID numérico
    List<Appointments> findByBarberIdId(Long barberId);

    // 4. Lista las citas de un barbero en un día específico (Para evitar colisiones de horarios)
    List<Appointments> findByBarberIdIdAndAppointmentDate(Long barberId, LocalDate appointmentDate);

    // 5. Lista las citas filtradas por un estado específico (ej: PENDIENTE, CANCELADA)
    List<Appointments> findByStatusIdId(Long statusId);
}
