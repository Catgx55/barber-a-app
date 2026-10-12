package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.RoleRequest;
import com.elcaballero.barberia.dto.ApiDtos.RoleResponse;
import com.elcaballero.barberia.entity.Role;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roles;

    public RoleService(RoleRepository roles) {
        this.roles = roles;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roles.findAll().stream().map(RoleService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return toResponse(EntityLookup.role(roles, id));
    }

    public RoleResponse create(RoleRequest request) {
        String name = normalize(request.name());
        if (roles.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un rol con ese nombre.");
        }
        Role role = new Role();
        role.setName(name);
        return toResponse(roles.save(role));
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role role = EntityLookup.role(roles, id);
        String name = normalize(request.name());
        if (!role.getName().equalsIgnoreCase(name) && roles.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un rol con ese nombre.");
        }
        role.setName(name);
        return toResponse(roles.save(role));
    }

    public void delete(Long id) {
        roles.delete(EntityLookup.role(roles, id));
    }

    private static String normalize(String name) {
        return name.trim();
    }

    private static RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName());
    }
}
