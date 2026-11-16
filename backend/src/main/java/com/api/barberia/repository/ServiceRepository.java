package com.api.barberia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.barberia.model.Services;

public interface ServiceRepository extends JpaRepository<Services, Long> {
    
    // 1. Busca un servicio exactamente por su nombre
    Optional<Services> findByName(String name);

    // 2. Filtra y devuelve solo los servicios que están activos
    List<Services> findByIsActiveTrue();

    // 3. Busca servicios que tengan una duración menor o igual a cierto tiempo
    List<Services> findByDurationMinutesLessThanEqual(Integer minutes);
}
