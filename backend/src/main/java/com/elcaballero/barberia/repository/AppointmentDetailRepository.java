package com.elcaballero.barberia.repository;

import com.elcaballero.barberia.entity.AppointmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetail, Long> {
}
