package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.UserRequest;
import com.elcaballero.barberia.dto.ApiDtos.UserResponse;
import com.elcaballero.barberia.entity.Role;
import com.elcaballero.barberia.entity.User;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.repository.RoleRepository;
import com.elcaballero.barberia.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return users.findAll().stream().map(UserService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(EntityLookup.user(users, id));
    }

    public UserResponse create(UserRequest request) {
        validatePassword(request.password());
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe un usuario con ese correo electrónico.");
        }

        User user = new User();
        apply(user, request, EntityLookup.role(roles, request.roleId()));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(users.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        validatePassword(request.password());
        User user = EntityLookup.user(users, id);
        String email = normalizeEmail(request.email());
        if (!user.getEmail().equalsIgnoreCase(email) && users.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("Ya existe un usuario con ese correo electrónico.");
        }

        apply(user, request, EntityLookup.role(roles, request.roleId()));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(users.save(user));
    }

    public void delete(Long id) {
        users.delete(EntityLookup.user(users, id));
    }

    private void apply(User user, UserRequest request, Role role) {
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalizeEmail(request.email()));
        user.setPhone(request.phone().trim());
        user.setRole(role);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validatePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new com.elcaballero.barberia.exception.BadRequestException(
                    "La contraseña no puede superar los 72 bytes en UTF-8.");
        }
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
