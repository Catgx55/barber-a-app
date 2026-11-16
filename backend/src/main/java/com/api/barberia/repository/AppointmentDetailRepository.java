package com.api.barberia.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.api.barberia.model.AppointmentDetails;

public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetails, Long> {
    
    // 1. Obtiene todos los detalles o servicios asociados a una cita específica por su ID
    List<AppointmentDetails> findByAppointmentIdId(Long appointmentId);

    // 2. Encuentra en qué detalles de cita se ha vendido un servicio en específico
    List<AppointmentDetails> findByServiceIdId(Long serviceId);

    // 3. Elimina de forma masiva todos los detalles asociados a una cita (Útil si se cancela o rehace la cita)
    void deleteByAppointmentIdId(Long appointmentId);
}
