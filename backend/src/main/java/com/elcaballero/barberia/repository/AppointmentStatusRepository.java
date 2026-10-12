package com.elcaballero.barberia.repository;

import com.elcaballero.barberia.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentStatusRepository extends JpaRepository<AppointmentStatus, Long> {
    boolean existsByNameIgnoreCase(String name);
}
