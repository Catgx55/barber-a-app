package com.api.barberia.controller;

import com.api.barberia.dto.RoleDTO;
import com.api.barberia.model.Roles;
import com.api.barberia.service.RoleService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Endpoint GET para listar todos los roles del sistema. 
    @GetMapping
    public ResponseEntity<List<Roles>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // Endpoint POST para registrar un nuevo rol.
    @PostMapping
    public ResponseEntity<Roles> createRole(@Valid @RequestBody RoleDTO dto) {
        Roles newRole = roleService.createRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
    }
}
