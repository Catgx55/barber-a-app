package com.api.barberia.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.api.barberia.model.Barbers;
import jakarta.persistence.LockModeType;

public interface BarberRepository extends JpaRepository<Barbers, Long> {
    
    // 1. Busca un barbero por el ID de su usuario asociado
    Optional<Barbers> findByUserIdId(Long userId);

    // Serialize bookings for the same barber while checking the requested time slot.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select barber from Barbers barber where barber.id = :id")
    Optional<Barbers> findByIdForUpdate(@Param("id") Long id);

    // 2. Filtra y lista únicamente los barberos que esta activos en el sistema
    List<Barbers> findByIsActiveTrue();

    // 3. Busca barberos que tengan una especialidad especifica
    List<Barbers> findBySpecialtyContainingIgnoreCase(String specialty);
}
