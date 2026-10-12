package com.elcaballero.barberia.controller;

import com.elcaballero.barberia.dto.ApiDtos.RoleRequest;
import com.elcaballero.barberia.dto.ApiDtos.RoleResponse;
import com.elcaballero.barberia.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roles;

    public RoleController(RoleService roles) {
        this.roles = roles;
    }

    @GetMapping
    public List<RoleResponse> findAll() {
        return roles.findAll();
    }

    @GetMapping("/{id}")
    public RoleResponse findById(@PathVariable Long id) {
        return roles.findById(id);
    }

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse created = roles.create(request);
        return ResponseEntity.created(URI.create("/api/roles/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roles.update(id, request);
    }

    @PatchMapping("/{id}")
    public RoleResponse patch(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roles.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roles.delete(id);
        return ResponseEntity.noContent().build();
    }
}
