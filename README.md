# El Caballero Barbería

Interfaz en React y Vite para mostrar el catálogo, las reseñas, los medios de pago, las reservas y la agenda de los barberos. La aplicación no usa backend ni se conecta a una base de datos: las citas y sus estados se guardan localmente en el navegador.

## Requisitos

- Node.js 20 o posterior.

## Iniciar en desarrollo
```sh
npm install
npm run dev
```

Abre la dirección que indica Vite, normalmente `http://localhost:5173`. Las reservas se almacenan en el `localStorage` del navegador y permanecen allí después de recargar la página; no se comparten con otros dispositivos.

## Funcionalidades

- Tema claro y oscuro.
- Catálogo de servicios incluido en la aplicación.
- Reserva de citas con validación de horarios, control de cruces y almacenamiento local.
- Panel del barbero para consultar las citas y actualizar su estado.
- Reseñas de clientes y métodos de pago disponibles.

La interfaz tiene exactamente cinco componentes React, separados en `src/components`: `Servicios`, `Reseña`, `MedioPago`, `AgendarCita` y `PanelBarbero`.
