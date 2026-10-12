package com.elcaballero.barberia.repository;

import com.elcaballero.barberia.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    boolean existsByNameIgnoreCase(String name);
}
