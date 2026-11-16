package com.api.barberia.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.api.barberia.dto.RoleDTO;
import com.api.barberia.model.Roles;
import com.api.barberia.repository.RoleRepository;

@Service
public class RoleService {
    
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Obtiene la lista completa de todos los roles registrados en el sistema.
    public List<Roles> getAllRoles() {
        return roleRepository.findAll();
    }

    // Registra un nuevo rol validando que su nombre no exista previamente.
    public Roles createRole(RoleDTO dto) {
        String roleName = dto.getName().trim().toUpperCase(java.util.Locale.ROOT);
        if(roleRepository.findByNameIgnoreCase(roleName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El rol ya existe.");
        }
        Roles role = new Roles();
        role.setName(roleName);
        return roleRepository.save(role);
    }
}
