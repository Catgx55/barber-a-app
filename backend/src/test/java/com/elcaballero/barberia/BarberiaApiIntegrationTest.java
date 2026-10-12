package com.elcaballero.barberia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:barberia-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.sql.init.mode=always"
        }
)
class BarberiaApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate http;

    @Test
    void managesCatalogUsersBarbersAndAppointmentsWithoutExposingPasswords() {
        ResponseEntity<List> seededServices = http.getForEntity("/api/services", List.class);
        assertThat(seededServices.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(seededServices.getBody()).hasSize(6);

        Map<?, ?> role = create("/api/roles", Map.of("name", "BARBER_TEST"));
        Map<?, ?> status = create("/api/appointment-statuses", Map.of("name", "CONFIRMADA_TEST"));
        Map<?, ?> service = create("/api/services", Map.of(
                "name", "Fade de prueba",
                "description", "Servicio para probar reservas.",
                "price", 25000,
                "durationMinutes", 45,
                "active", true
        ));

        Map<?, ?> user = create("/api/users", Map.of(
                "firstName", "Carlos",
                "lastName", "Barbero",
                "email", "carlos@example.com",
                "phone", "+573001234567",
                "password", "prueba-segura-123",
                "roleId", role.get("id")
        ));
        assertThat(user.containsKey("password")).isFalse();

        Map<?, ?> barber = create("/api/barbers", Map.of(
                "userId", user.get("id"),
                "specialty", "Fade",
                "active", true
        ));

        LocalDate date = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
        Map<?, ?> appointment = create("/api/appointments", Map.of(
                "clientId", user.get("id"),
                "barberId", barber.get("id"),
                "statusId", status.get("id"),
                "appointmentDate", date,
                "startTime", LocalTime.of(11, 0),
                "notes", "Primera cita",
                "serviceIds", List.of(service.get("id"))
        ));

        assertThat(new BigDecimal(appointment.get("totalAmount").toString()))
                .isEqualByComparingTo("25000");
        assertThat(((List<?>) appointment.get("details"))).hasSize(1);

        Map<?, ?> beardService = create("/api/services", Map.of(
                "name", "Perfilado de prueba",
                "description", "Servicio adicional para probar el detalle.",
                "price", 12000,
                "durationMinutes", 20,
                "active", true
        ));
        Map<?, ?> detail = create("/api/appointment-details", Map.of(
                "appointmentId", appointment.get("id"),
                "serviceId", beardService.get("id")
        ));
        Map<?, ?> appointmentWithExtraService = http.getForObject(
                "/api/appointments/" + appointment.get("id"), Map.class);
        assertThat(new BigDecimal(appointmentWithExtraService.get("totalAmount").toString()))
                .isEqualByComparingTo("37000");
        assertThat(appointmentWithExtraService.get("endTime").toString()).isEqualTo("12:05:00");

        ResponseEntity<Map> updatedDetail = http.exchange(
                "/api/appointment-details/" + detail.get("id"),
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of(
                        "appointmentId", appointment.get("id"),
                        "serviceId", beardService.get("id")
                )),
                Map.class
        );
        assertThat(updatedDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        http.delete("/api/appointment-details/" + detail.get("id"));
        Map<?, ?> appointmentAfterRemovingDetail = http.getForObject(
                "/api/appointments/" + appointment.get("id"), Map.class);
        assertThat(new BigDecimal(appointmentAfterRemovingDetail.get("totalAmount").toString()))
                .isEqualByComparingTo("25000");

        ResponseEntity<Map> overlap = http.postForEntity("/api/appointments", new HttpEntity<>(Map.of(
                "clientId", user.get("id"),
                "barberId", barber.get("id"),
                "statusId", status.get("id"),
                "appointmentDate", date,
                "startTime", LocalTime.of(11, 30),
                "notes", "",
                "serviceIds", List.of(service.get("id"))
        )), Map.class);
        assertThat(overlap.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Map> serviceUpdate = http.exchange(
                "/api/services/" + service.get("id"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of(
                        "name", "Fade de prueba actualizado",
                        "description", "Precio actualizado.",
                        "price", 30000,
                        "durationMinutes", 50,
                        "active", true
                )),
                Map.class
        );
        assertThat(serviceUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> updatedService = serviceUpdate.getBody();
        assertThat(updatedService).isNotNull();
        assertThat(new BigDecimal(updatedService.get("price").toString()))
                .isEqualByComparingTo("30000");

        ResponseEntity<Void> cors = http.exchange(
                RequestEntity.options(URI.create("http://localhost:" + port + "/api/services"))
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .build(),
                Void.class
        );
        assertThat(cors.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cors.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://localhost:5173");

        http.delete("/api/appointments/" + appointment.get("id"));
        http.delete("/api/services/" + service.get("id"));
        ResponseEntity<Map> inactiveService = http.getForEntity("/api/services/" + service.get("id"), Map.class);
        assertThat(inactiveService.getBody()).containsEntry("active", false);
    }

    private Map<?, ?> create(String path, Map<String, ?> payload) {
        ResponseEntity<Map> response = http.postForEntity(path, new HttpEntity<>(payload), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
