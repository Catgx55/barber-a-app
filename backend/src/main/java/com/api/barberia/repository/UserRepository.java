package com.api.barberia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import com.api.barberia.model.Users;


public interface UserRepository extends JpaRepository<Users, Long> {
    
    // 1. Verifica si un correo ya existe en la base de datos
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // 2. Busca un usuario por su correo electronico
    @EntityGraph(attributePaths = "rolesId")
    Optional<Users> findByEmailIgnoreCase(String email);

    // 3. Busca un usuario por su numero de telefono
    Optional<Users> findByPhone(String phone);
}
