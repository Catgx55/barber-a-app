# El Caballero Barbería

Interfaz en React y Vite para mostrar el catálogo, las reseñas, los medios de pago, las reservas y la agenda de los barberos. Incluye una API REST Spring Boot para administrar el catálogo y guardar los datos en MySQL. La interfaz actual conserva además su almacenamiento local del navegador.

## Requisitos

- Node.js 20 o posterior.
- Java 21.
- Maven 3.9 o posterior.
- MySQL 8.

## Preparar MySQL

```sql
CREATE DATABASE barberia_el_caballero
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Configura las credenciales por variables de entorno; no almacenes contraseñas en el repositorio:

```sh
export DB_URL='jdbc:mysql://localhost:3306/barberia_el_caballero?serverTimezone=America/Bogota'
export DB_USERNAME='tu_usuario'
export DB_PASSWORD='tu_contraseña'
```

Al arrancar, `backend/src/main/resources/schema.sql` crea las tablas y `data.sql` inserta los roles, estados y seis servicios iniciales. Hibernate valida el esquema existente y no lo modifica automáticamente.

## Iniciar la API

```sh
cd backend
mvn spring-boot:run
```

La API escucha en `http://localhost:8080`. Para compilar y ejecutar sus pruebas:

```sh
cd backend
mvn test
```

## Iniciar el frontend

En otra terminal, desde la raíz:

```sh
npm install
npm run dev
```

Abre la dirección que indica Vite, normalmente `http://localhost:5173`. El proxy de Vite reenvía `/api` a Spring Boot. El frontend actual conserva su propia agenda en `localStorage`; el backend ofrece sus endpoints para conectar ambas capas.

## Funcionalidades

- Tema claro y oscuro.
- API CRUD para roles, servicios, estados de citas, usuarios, barberos, citas y sus detalles.
- Validación de reservas y prevención de horarios superpuestos por barbero.
- Contraseñas cifradas con BCrypt y respuestas de usuarios sin el campo de contraseña.
- Desactivación de servicios y barberos para preservar el historial de citas.
- CORS configurado para `http://localhost:5173`.
- Panel del barbero para consultar las citas y actualizar su estado.
- Reseñas de clientes y métodos de pago disponibles.

La interfaz tiene exactamente cinco componentes React, separados en `src/components`: `Servicios`, `Reseña`, `MedioPago`, `AgendarCita` y `PanelBarbero`.

## Endpoints REST

Los siete recursos principales exponen `GET /api/{recurso}`, `GET /api/{recurso}/{id}`, `POST /api/{recurso}`, `PUT /api/{recurso}/{id}`, `PATCH /api/{recurso}/{id}` y `DELETE /api/{recurso}/{id}`:

| Recurso | Ruta | Observaciones |
| --- | --- | --- |
| Roles | `/api/roles` | CRUD completo; la eliminación responde `409` si el rol está en uso. |
| Servicios | `/api/services` | `DELETE` desactiva el servicio para conservar el historial. |
| Estados de cita | `/api/appointment-statuses` | Incluye estados iniciales como `CONFIRMADA` y `CANCELADA`. |
| Usuarios | `/api/users` | La contraseña se cifra; no se devuelve en las respuestas. |
| Barberos | `/api/barbers` | `DELETE` desactiva el perfil. |
| Citas | `/api/appointments` | Comprueba disponibilidad y calcula el precio y la duración desde el catálogo. |
| Detalles de cita | `/api/appointment-details` | Crear, editar o borrar detalles recalcula el importe y la duración de la cita. |

Los cuerpos de `PUT` y `PATCH` son representaciones completas y siguen las mismas reglas de validación. La creación de citas acepta identificadores de servicio —nunca precios arbitrarios—, por ejemplo:

```json
{
  "clientId": 1,
  "barberId": 1,
  "statusId": 2,
  "appointmentDate": "2026-12-12",
  "startTime": "10:00",
  "notes": "Corte clásico",
  "serviceIds": [1, 2]
}
```

**Seguridad:** las contraseñas de usuarios se guardan con BCrypt y nunca se devuelven por la API. Este CRUD no implementa autenticación ni autorización; antes de publicarlo en internet, protege sus rutas administrativas con autenticación y control de acceso.
