# Barbería El Caballero

Aplicación web para **Barbería El Caballero**, en Armenia, Quindío. Incluye una interfaz React/Vite y una API REST con Spring Boot, Java y JPA. El catálogo, las cuentas del personal y las reservas se gestionan mediante MySQL.

## Contenido

- [Tecnologías y requisitos](#tecnologías-y-requisitos)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Puesta en marcha](#puesta-en-marcha)
- [Frontend](#frontend)
- [Backend y MySQL](#backend-y-mysql)
- [Modelo de datos](#modelo-de-datos)
- [API REST y permisos](#api-rest-y-permisos)
- [Pruebas](#pruebas)
- [Limitaciones y seguridad antes de producción](#limitaciones-y-seguridad-antes-de-producción)

## Tecnologías y requisitos

- Node.js 20 o posterior y npm.
- Java 25 (LTS).
- Maven 3.9 o posterior.
- MySQL 8.
- React 18 y Vite 6 (frontend); Spring Boot 4.2 snapshot (backend, como en la API de origen).

## Puesta en marcha

### 1. Preparar MySQL

Crea la base de datos y, si todavía no existe, un usuario local para la aplicación. Sustituye el valor de ejemplo por una contraseña nueva; no uses credenciales compartidas en mensajes ni guardes la contraseña en Git.

```sql
CREATE DATABASE IF NOT EXISTS barberia_el_caballero_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'api_barber'@'localhost'
  IDENTIFIED BY 'REEMPLAZA_CON_UNA_CLAVE_SEGURA';
GRANT ALL PRIVILEGES ON barberia_el_caballero_db.*
  TO 'api_barber'@'localhost';
```

Si MySQL y la API se ejecutan en máquinas distintas, configura el usuario para el host de origen correcto y restringe el acceso de red. No habilites acceso remoto indiscriminado.

### 2. Configurar e iniciar el backend

Desde la raíz del repositorio, exporta la contraseña de MySQL y las credenciales iniciales del administrador. La contraseña del administrador debe tener entre 12 y 72 bytes en UTF-8.

```sh
read -rsp 'Contraseña MySQL: ' DB_PASSWORD
printf '\n'
export DB_PASSWORD

read -rp 'Correo del administrador inicial: ' APP_ADMIN_EMAIL
read -rsp 'Contraseña inicial del administrador: ' APP_ADMIN_PASSWORD
printf '\n'
export APP_ADMIN_EMAIL APP_ADMIN_PASSWORD

cd backend
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Mantén esta terminal abierta.

### 3. Instalar e iniciar el frontend

En una segunda terminal, desde la raíz del repositorio:

```sh
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite reenvía las solicitudes `/api` al backend local en el puerto `8080`.

### Crear cuentas para iniciar sesión

No existe una pantalla de registro de personal en la interfaz. El acceso se prepara así:

1. **Administrador:** al iniciar el backend, se crea automáticamente si configuraste `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` antes de ejecutar `mvn spring-boot:run` (paso 2). Si el backend ya estaba iniciado sin esas variables, detenlo y vuelve a iniciarlo con ellas. Si ese correo ya pertenece a un administrador, el arranque conserva su contraseña actual; no la restablece.
2. **Barbero:** primero inicia sesión como administrador y crea un usuario con rol `BARBER`; después crea el perfil de barbero enlazado a ese usuario. Ambos pasos se hacen en la API (por ejemplo, desde una terminal con `curl` o desde Postman).

Consulta los roles disponibles. En la respuesta, localiza el objeto cuyo `name` sea `BARBER` y copia su `id`:

```sh
curl -u "$APP_ADMIN_EMAIL:$APP_ADMIN_PASSWORD" \
  http://localhost:8080/api/roles
```

Crea el usuario del barbero en `POST /api/users/register`, reemplazando `ID_DEL_ROL_BARBER` y los datos de ejemplo. La contraseña debe tener entre 8 y 72 bytes UTF-8. Anota el `id` que devuelve la respuesta:

```sh
curl -X POST http://localhost:8080/api/users/register \
  -u "$APP_ADMIN_EMAIL:$APP_ADMIN_PASSWORD" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Juan","lastName":"Pérez","email":"juan@example.com","phone":"+573001234567","password":"CAMBIA_ESTA_CLAVE","rolesId":ID_DEL_ROL_BARBER}'
```

Con ese ID, crea el perfil en `POST /api/barbers`:

```sh
curl -X POST http://localhost:8080/api/barbers \
  -u "$APP_ADMIN_EMAIL:$APP_ADMIN_PASSWORD" \
  -H 'Content-Type: application/json' \
  -d '{"userId":ID_DEL_USUARIO,"specialty":"Cortes clásicos y degradados","isActive":true}'
```

Después, el barbero puede iniciar sesión en el panel con el correo y la contraseña asignados al crear su usuario. `ID_DEL_ROL_BARBER` e `ID_DEL_USUARIO` son marcadores: sustitúyelos por los números reales de las respuestas, sin comillas. Nunca compartas ni reutilices las contraseñas de ejemplo.

Para una comprobación rápida del catálogo:

```sh
curl http://localhost:8080/api/services
```

## Estructura del repositorio

```text
.
├── index.html
├── src/
│   ├── main.jsx
│   ├── api.js
│   ├── data.js
│   ├── styles.css
│   └── components/
│       ├── AgendarCita.jsx
│       ├── MedioPago.jsx
│       ├── PanelBarbero.jsx
│       ├── Resena.jsx
│       └── Servicios.jsx
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/api/barberia/
│       │   ├── config/       # Seguridad, CORS y datos iniciales
│       │   ├── controller/   # Rutas REST y manejo de errores HTTP
│       │   ├── dto/          # Contratos JSON de solicitudes/respuestas
│       │   ├── model/        # Entidades JPA
│       │   ├── repository/   # Consultas Spring Data JPA
│       │   └── service/      # Reglas de negocio y persistencia
│       ├── main/resources/application.properties
│       └── test/             # Pruebas con H2
├── package.json
└── vite.config.js
```

## Frontend

`src/main.jsx` monta `AgendarCita`, que organiza la página y el acceso al panel. Los cinco componentes solicitados viven en `src/components`. `src/api.js` centraliza las peticiones HTTP y la autenticación del personal. `src/data.js` contiene imágenes ilustrativas asociadas por nombre de servicio, métodos de pago y formato COP. El tema, los estilos adaptables y el diseño están en `src/styles.css`.

Durante el desarrollo, Vite se ejecuta en `http://localhost:5173` (consulta [Puesta en marcha](#puesta-en-marcha)). Para generar y previsualizar una compilación de producción:

```sh
npm run build
npm run preview
```

La página obtiene servicios activos y barberos activos desde la API. Las reservas se crean en MySQL mediante `POST /api/public/appointments`, asociadas a un cliente por correo y con estado inicial `PENDIENTE`; errores de disponibilidad y validación del servidor se muestran en el formulario. El medio de pago seleccionado también se persiste como dato informativo: no se cobra en línea. El panel exige una cuenta `BARBER` o `ADMIN`, carga citas y estados protegidos, y envía los cambios de estado al servidor. Las credenciales Basic solo viven en la memoria del componente mientras el panel está montado y se transmiten en cada petición.

El proxy `/api` de `vite.config.js` reenvía las peticiones de Vite a `http://localhost:8080`. Las reseñas siguen siendo contenido estático porque el esquema no contempla reseñas. Las fotografías y las fuentes se cargan desde servicios externos.

## Backend y MySQL

La API actual parte de [Catgx55/api_barberia](https://github.com/Catgx55/api_barberia), integrada y extendida para conservar la conexión con este frontend. Su configuración está en `backend/src/main/resources/application.properties`. Los valores predeterminados de desarrollo son:

- Base de datos: `barberia_el_caballero_db`.
- Host/puerto: `localhost:3306`.
- Usuario: `barberia_user`.
- Contraseña: variable obligatoria `DB_PASSWORD`, nunca almacenada en el repositorio.
- Hibernate: `ddl-auto=update`.
- Zona horaria: `America/Bogota`.

Puedes sobrescribir la conexión con estas variables de entorno:

| Variable | Uso |
| --- | --- |
| `DB_URL` | URL JDBC; por defecto apunta a MySQL local en el puerto `3306`. |
| `DB_USERNAME` | Usuario de MySQL; por defecto `barberia_user`. |
| `DB_PASSWORD` | Contraseña obligatoria de MySQL. |
| `PORT` | Puerto HTTP de Spring Boot; por defecto `8080`. |
| `APP_ADMIN_EMAIL` | Correo para crear el administrador inicial si no existe. |
| `APP_ADMIN_PASSWORD` | Contraseña inicial; entre 12 y 72 bytes UTF-8. |

La URL predeterminada es para desarrollo local. En producción, configura `DB_URL` con `sslMode=VERIFY_IDENTITY` y el certificado CA/identidad correcto; no expongas credenciales o datos mediante una conexión remota sin TLS verificado.

Si ambas variables de administrador se configuran y la cuenta no existe, el arranque crea el rol `ADMIN` si hace falta y guarda la contraseña con BCrypt. Si el correo ya pertenece a una cuenta `ADMIN`, su contraseña existente no se reemplaza. Configurar solo una de las dos variables o usar una contraseña fuera del rango permitido impide el arranque.

En el arranque, `BootstrapDataInitializer` crea de forma idempotente los roles, estados de cita y seis servicios iniciales; también crea el administrador inicial si se configuraron ambas variables de cuenta. Hibernate crea o actualiza las tablas según las entidades. `ddl-auto=update` es práctico para desarrollo, pero no sustituye migraciones versionadas para producción: respalda la base antes de actualizar y valida cualquier DDL preexistente.

### Modelo de datos

| Tabla | Relaciones y comportamiento |
| --- | --- |
| `roles` | Roles de usuario; nombre único. |
| `users` | Pertenece a un rol; correo único normalizado; contraseñas BCrypt. |
| `barbers` | Perfil uno-a-uno con un usuario; la baja lógica conserva el historial. |
| `services` | Catálogo con precio y duración; la baja lógica conserva los detalles de citas. |
| `appointment_statuses` | Estados iniciales: `PENDIENTE`, `CONFIRMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA`. |
| `appointments` | Referencia cliente, barbero y estado; contiene fecha, hora, total, notas y el medio de pago elegido. |
| `appointment_details` | Une cita y servicio y conserva el precio aplicado al reservar. Los detalles no se pueden repetir en una cita. |

Las entidades y relaciones JPA se encuentran en `backend/src/main/java/com/api/barberia/model`. La columna de rol se mapea a `users.role_id`, compatible con el esquema usado previamente por este proyecto. La reserva añade la columna `payment_method` a `appointments` para guardar la preferencia de pago; no se procesa ningún cobro.

### API REST y permisos

Las rutas administrativas requieren HTTP Basic. Las operaciones disponibles y sus permisos son:

| Recurso | Rutas | Permiso |
| --- | --- | --- |
| Roles | `GET, POST /api/roles` | Solo `ADMIN`. |
| Servicios | `GET /api/services`, `GET /api/services/active`; `POST`, `PUT /{id}`, `DELETE /{id}` | Lecturas públicas; escritura solo `ADMIN`. |
| Usuarios | `GET /api/users`, `GET /{id}`, `POST /register`, `PUT /{id}`, `DELETE /{id}` | Solo `ADMIN`. |
| Barberos | `GET /api/barbers`, `GET /active`, `POST`, `PUT /{id}`, `DELETE /{id}` | Solo `ADMIN`. La baja desactiva el perfil para conservar su historial. |
| Estados | `GET /api/appointment-statuses` | `BARBER`/`ADMIN`; los estados se inicializan al arrancar. |
| Citas | `GET /api/appointments`, `GET /{id}`, `GET /date/{yyyy-MM-dd}` | `ADMIN` ve todas; `BARBER` solo las propias. |
| Citas | `POST /api/appointments`, `PUT /{id}`, `DELETE /{id}` | Solo `ADMIN`. |
| Estado de cita | `PATCH /api/appointments/{id}/status` | `BARBER`/`ADMIN`; un barbero solo puede modificar sus citas. |

Rutas de integración utilizadas por el frontend:

| Método y ruta | Acceso | Propósito |
| --- | --- | --- |
| `GET /api/services` | Público | Catálogo; la interfaz muestra únicamente los servicios activos (`isActive: true`). |
| `GET /api/public/barbers` | Público | ID, nombre y especialidad de barberos activos. |
| `POST /api/public/appointments` | Público | Registra/recupera cliente por correo y crea reserva `PENDIENTE`. |
| `GET /api/auth/me` | `BARBER`/`ADMIN` | Valida credenciales y devuelve identidad/roles. |
| `GET /api/appointments` | `BARBER`/`ADMIN` | Consulta la agenda. |
| `GET /api/appointment-statuses` | `BARBER`/`ADMIN` | Carga estados para el panel. |
| `PATCH /api/appointments/{id}/status` | `BARBER`/`ADMIN` | Cambia únicamente el estado de una cita. |

La API utiliza HTTP Basic sin estado y BCrypt. El panel transmite el encabezado `Authorization` desde el navegador; no conserva la clave en `localStorage`. **Usa HTTPS en cualquier entorno que no sea desarrollo local.** Las rutas de administración requieren `ADMIN`; cada cuenta `BARBER` solo puede consultar las citas asociadas a su propio perfil y cambiar sus estados. Las rutas públicas de reserva no permiten leer la agenda ni los datos de los clientes.

Las actualizaciones administrativas usan `PUT` con el objeto completo. El `PATCH /api/appointments/{id}/status` acepta solo `{"statusId": 2}`. Las respuestas usan JSON camelCase. Códigos principales: `200` lectura/actualización, `201` creación, `204` eliminación, `400` validación/regla de negocio, `401` sin autenticación, `403` rol insuficiente, `404` recurso inexistente y `409` conflicto.

Ejemplo para crear un servicio:

```sh
curl -X POST http://localhost:8080/api/services \
  -u "$APP_ADMIN_EMAIL:$APP_ADMIN_PASSWORD" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Fade clásico","description":"Degradado a navaja.","price":25000,"durationMinutes":45,"isActive":true}'
```

Ejemplo de reserva pública (no incluye identificadores de usuario ni de estado; el backend crea o reutiliza al cliente y fija el estado inicial):

```sh
curl -X POST http://localhost:8080/api/public/appointments \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ana","lastName":"Pérez","email":"ana@example.com","phone":"+573001234567","barberId":1,"appointmentDate":"2026-12-12","startTime":"10:00","notes":"","serviceIds":[1],"paymentMethod":"NEQUI"}'
```

El cuerpo de la reserva que acepta el endpoint:

```json
{
  "barberId": 1,
  "firstName": "Ana",
  "lastName": "Pérez",
  "email": "ana@example.com",
  "phone": "+573001234567",
  "appointmentDate": "2026-12-12",
  "startTime": "10:00",
  "notes": "",
  "serviceIds": [1],
  "paymentMethod": "NEQUI"
}
```

El backend calcula el importe y la duración desde los servicios activos, comprueba que la cita sea futura, aplica el horario del negocio y rechaza solapamientos para un mismo barbero. Horario configurado: lunes a viernes 08:00–20:00, sábados 08:00–18:00 y domingos 10:00–16:00. `DELETE` de servicios y barberos desactiva el registro para conservar el historial.

CORS permite el origen `http://localhost:5173` y los métodos GET, POST, PUT, PATCH, DELETE y OPTIONS desde `ApiSecurityConfig`. Para otros dominios, modifica la lista explícita de orígenes allí.

Para preparar un barbero desde la API, autentícate como administrador, consulta `GET /api/roles` y crea el usuario con el ID de rol `BARBER` usando `POST /api/users/register`. Después crea su perfil en `/api/barbers` con ese `userId`, una `specialty` y `isActive: true`. La página pública solo muestra perfiles activos.

## Pruebas

Desde la raíz del repositorio:

```sh
npm run build
mvn -f backend/pom.xml test
```

La prueba de integración del backend ejecuta la aplicación contra H2 en modo MySQL. Verifica inicialización, autenticación, permisos, CORS, registro de barbero, reserva pública, conflicto de horario y cambio de estado; **no verifica** disponibilidad de un servidor MySQL real, permisos de usuario, credenciales ni TLS. Para probar la conexión real, inicia la API con las variables de entorno configuradas y consulta `/api/services`.

## Limitaciones y seguridad antes de producción

- Las reservas públicas aceptan el correo declarado por quien envía el formulario y pueden reutilizar un cliente con ese correo; el estado inicial pendiente requiere confirmación del personal. Sin verificación por correo/SMS, esto no demuestra la identidad del cliente.
- HTTP Basic envía credenciales en cada petición; despliega detrás de HTTPS y nunca lo uses sobre redes públicas sin TLS. Agrega limitación de intentos de autenticación antes de una exposición pública.
- Reseñas y procesamiento de pagos no tienen endpoints ni almacenamiento en el esquema actual; los medios de pago guardados son solo una preferencia declarada por el cliente.
- Protege MySQL con credenciales de mínimo privilegio, TLS verificado y copias de seguridad. No guardes secretos en archivos versionados.
- Antes de desplegar, reemplaza el esquema de actualización automática por migraciones controladas, establece una lista CORS de producción y configura HTTPS.
