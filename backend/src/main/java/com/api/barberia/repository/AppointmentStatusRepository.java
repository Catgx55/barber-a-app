package com.api.barberia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.barberia.model.AppointmentStatuses;

public interface AppointmentStatusRepository extends JpaRepository<AppointmentStatuses, Long> {
    
    // 1. Busca un estado por su nombre exacto (Ej: "CONFIRMADA")
    Optional<AppointmentStatuses> findByName(String name);

    Optional<AppointmentStatuses> findByNameIgnoreCase(String name);

    // 2. Comprueba si un estado ya existe en MySQL para evitar duplicados
    boolean existsByName(String name);
}
