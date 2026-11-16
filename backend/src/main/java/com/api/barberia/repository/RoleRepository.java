package com.api.barberia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.barberia.model.Roles;

public interface RoleRepository extends JpaRepository<Roles, Long> {
    
    // 1. Busca un rol por su nombre extacto (Ej: "ADMIN", "BARBER")
    Optional<Roles> findByName(String name);

    Optional<Roles> findByNameIgnoreCase(String name);

    // 2. Comprueba si un rol ya existe registrado con ese nombre
    boolean existsByName(String name);
}
