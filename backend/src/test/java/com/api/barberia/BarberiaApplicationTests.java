package com.api.barberia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.api.barberia.repository.AppointmentStatusRepository;
import com.api.barberia.repository.RoleRepository;
import com.api.barberia.repository.ServiceRepository;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:barberia;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "APP_ADMIN_EMAIL=admin@example.test",
        "APP_ADMIN_PASSWORD=ChangeMeTestPassword123"
})
class BarberiaApplicationTests {

    @Autowired
    private RoleRepository roles;

    @Autowired
    private AppointmentStatusRepository statuses;

    @Autowired
    private ServiceRepository services;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private MockMvc mvc;

    @Test
    void initializesCatalogAndAdminLogin() {
        assertTrue(roles.findByNameIgnoreCase("ADMIN").isPresent());
        assertTrue(roles.findByNameIgnoreCase("BARBER").isPresent());
        assertTrue(roles.findByNameIgnoreCase("CLIENT").isPresent());
        assertTrue(statuses.findByNameIgnoreCase("PENDIENTE").isPresent());
        assertEquals(6, services.findByIsActiveTrue().size());

        var administrator = userDetailsService.loadUserByUsername("admin@example.test");
        assertTrue(administrator.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void publicBookingAndStaffAuthenticationWorkWithRepositoryContracts() throws Exception {
        String adminAuthorization = basicAuthorization("admin@example.test", "ChangeMeTestPassword123");
        mvc.perform(get("/api/auth/me").header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.test"));
        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(true));
        mvc.perform(options("/api/public/appointments")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        Long barberRoleId = roles.findByNameIgnoreCase("BARBER").orElseThrow().getId();
        String createdUser = mvc.perform(post("/api/users/register")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName":"Barbero",
                                  "lastName":"Prueba",
                                  "phone":"+573001234567",
                                  "email":"barber@example.test",
                                  "password":"BarberTestPassword123",
                                  "rolesId":%d
                                }
                                """.formatted(barberRoleId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertTrue(!createdUser.contains("password"));
        Long userId = jsonId(createdUser);

        mvc.perform(post("/api/barbers")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"specialty":"Cortes clásicos","isActive":true,"userId":%d}
                                """.formatted(userId)))
                .andExpect(status().isCreated());

        Long serviceId = services.findByName("Fade clásico").orElseThrow().getId();
        LocalDate nextTuesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        String booking = """
                {
                  "firstName":"Cliente",
                  "lastName":"Prueba",
                  "email":"client@example.test",
                  "phone":"+573001234567",
                  "barberId":1,
                  "appointmentDate":"%s",
                  "startTime":"10:00",
                  "notes":"",
                  "serviceIds":[%d],
                  "paymentMethod":"NEQUI"
                }
                """.formatted(nextTuesday, serviceId);

        String appointment = mvc.perform(post("/api/public/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(booking))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusName").value("PENDIENTE"))
                .andExpect(jsonPath("$.paymentMethod").value("NEQUI"))
                .andExpect(jsonPath("$.details[0].serviceName").value("Fade clásico"))
                .andReturn().getResponse().getContentAsString();
        Long appointmentId = jsonId(appointment);

        mvc.perform(post("/api/public/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(booking))
                .andExpect(status().isConflict());

        String barberAuthorization = basicAuthorization("barber@example.test", "BarberTestPassword123");
        Long confirmedStatusId = statuses.findByNameIgnoreCase("CONFIRMADA").orElseThrow().getId();
        mvc.perform(patch("/api/appointments/{id}/status", appointmentId)
                        .header("Authorization", barberAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusId\":" + confirmedStatusId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusName").value("CONFIRMADA"));
        mvc.perform(get("/api/appointments").header("Authorization", barberAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Cliente Prueba"));
    }

    private static String basicAuthorization(String email, String password) {
        String credentials = email + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static Long jsonId(String json) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new AssertionError("Expected an id in the API response.");
        }
        return Long.valueOf(matcher.group(1));
    }
}
