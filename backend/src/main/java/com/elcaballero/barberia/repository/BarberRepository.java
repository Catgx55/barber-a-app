package com.elcaballero.barberia.repository;

import com.elcaballero.barberia.entity.Barber;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BarberRepository extends JpaRepository<Barber, Long> {
    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndIdNot(Long userId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Barber b where b.id = :id")
    Optional<Barber> findByIdForUpdate(@Param("id") Long id);

    @Query("select b from Barber b join fetch b.user where b.id = :id")
    Optional<Barber> findWithUserById(@Param("id") Long id);

    @Query("select b from Barber b join fetch b.user")
    java.util.List<Barber> findAllWithUsers();
}
