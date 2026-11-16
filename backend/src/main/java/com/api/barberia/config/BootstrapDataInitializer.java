package com.api.barberia.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.api.barberia.model.AppointmentStatuses;
import com.api.barberia.model.Roles;
import com.api.barberia.model.Services;
import com.api.barberia.model.Users;
import com.api.barberia.repository.AppointmentStatusRepository;
import com.api.barberia.repository.RoleRepository;
import com.api.barberia.repository.ServiceRepository;
import com.api.barberia.repository.UserRepository;

/**
 * Adds the minimum reference data needed by the booking flow and optionally
 * creates the first administrator from environment variables.
 */
@Component
public class BootstrapDataInitializer implements ApplicationRunner {

    private final RoleRepository roles;
    private final AppointmentStatusRepository statuses;
    private final ServiceRepository services;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public BootstrapDataInitializer(
            RoleRepository roles,
            AppointmentStatusRepository statuses,
            ServiceRepository services,
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${APP_ADMIN_EMAIL:}") String adminEmail,
            @Value("${APP_ADMIN_PASSWORD:}") String adminPassword) {
        this.roles = roles;
        this.statuses = statuses;
        this.services = services;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedStatuses();
        seedServices();
        createInitialAdmin();
    }

    private void seedRoles() {
        for (String name : List.of("ADMIN", "BARBER", "CLIENT")) {
            if (roles.findByNameIgnoreCase(name).isEmpty()) {
                Roles role = new Roles();
                role.setName(name);
                roles.save(role);
            }
        }
    }

    private void seedStatuses() {
        for (String name : List.of("PENDIENTE", "CONFIRMADA", "EN_CURSO", "FINALIZADA", "CANCELADA")) {
            if (statuses.findByNameIgnoreCase(name).isEmpty()) {
                AppointmentStatuses status = new AppointmentStatuses();
                status.setName(name);
                statuses.save(status);
            }
        }
    }

    private void seedServices() {
        List<SeedService> defaults = List.of(
                new SeedService("Fade clásico", "Degradado a navaja, precisión en cada detalle.", 25000, 45),
                new SeedService("Corte tradicional", "El corte de siempre: tijera, peine y mucho estilo.", 20000, 30),
                new SeedService("Mullet moderno", "Un estilo retro reinventado y a tu medida.", 30000, 60),
                new SeedService("Degradado + diseño", "Degradado con diseño personalizado a navaja.", 35000, 75),
                new SeedService("Afeitado con navaja", "Toalla caliente y un afeitado clásico y relajante.", 18000, 35),
                new SeedService("Corte + perfilado", "Corte completo con perfilado de barba y contornos.", 28000, 50));

        for (SeedService seed : defaults) {
            if (services.findByName(seed.name()).isEmpty()) {
                Services service = new Services();
                service.setName(seed.name());
                service.setDescription(seed.description());
                service.setPrice(java.math.BigDecimal.valueOf(seed.price()));
                service.setDurationMinutes(seed.durationMinutes());
                service.setIsActive(true);
                services.save(service);
            }
        }
    }

    private void createInitialAdmin() {
        if (adminEmail.isBlank() && adminPassword.isBlank()) {
            return;
        }
        int passwordBytes = adminPassword.getBytes(StandardCharsets.UTF_8).length;
        if (adminEmail.isBlank() || passwordBytes < 12 || passwordBytes > 72) {
            throw new IllegalStateException(
                    "Configura APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD entre 12 y 72 bytes UTF-8.");
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        var existing = users.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isPresent()) {
            if (!"ADMIN".equalsIgnoreCase(existing.get().getRolesId().getName())) {
                throw new IllegalStateException("APP_ADMIN_EMAIL ya pertenece a una cuenta que no es ADMIN.");
            }
            return;
        }

        Users admin = new Users();
        admin.setFirstName("Administrador");
        admin.setLastName("Barbería");
        admin.setPhone("0000000000");
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRolesId(roles.findByNameIgnoreCase("ADMIN")
                .orElseThrow(() -> new IllegalStateException("No se pudo crear el rol ADMIN.")));
        users.save(admin);
    }

    private record SeedService(String name, String description, int price, int durationMinutes) {
    }
}
