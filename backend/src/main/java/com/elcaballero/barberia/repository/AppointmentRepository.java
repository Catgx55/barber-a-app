package com.elcaballero.barberia.repository;

import com.elcaballero.barberia.entity.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByBarberId(Long barberId);

    @Override
    @EntityGraph(attributePaths = {
            "client", "barber", "barber.user", "status", "details", "details.service"
    })
    java.util.List<Appointment> findAll();

    @Query("""
            select case when count(a) > 0 then true else false end
            from Appointment a
            where a.barber.id = :barberId
              and a.appointmentDate = :appointmentDate
              and upper(a.status.name) <> 'CANCELADA'
              and a.startTime < :endTime
              and a.endTime > :startTime
              and (:excludeId is null or a.id <> :excludeId)
            """)
    boolean existsOverlappingAppointment(
            @Param("barberId") Long barberId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
