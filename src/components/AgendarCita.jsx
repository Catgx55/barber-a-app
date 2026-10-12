import { useEffect, useState } from 'react';
import MedioPago from './MedioPago.jsx';
import PanelBarbero from './PanelBarbero.jsx';
import Reseña from './Resena.jsx';
import Servicios from './Servicios.jsx';
import { BARBEROS, CLAVE_CITAS, COP, METODOS_PAGO, SERVICIOS } from '../data.js';

const FORMULARIO_VACIO = {
  cliente: '',
  telefono: '',
  correo: '',
  servicioId: '',
  barbero: '',
  fechaHora: '',
  metodoPago: '',
};

function leerCitas() {
  try {
    const guardadas = localStorage.getItem(CLAVE_CITAS);
    if (!guardadas) return { citas: [], error: '' };

    const citas = JSON.parse(guardadas);
    if (!Array.isArray(citas)) throw new Error('El formato guardado no es válido.');
    return { citas, error: '' };
  } catch (error) {
    return {
      citas: [],
      error: `No se pudieron leer las reservas guardadas: ${error.message}`,
    };
  }
}

function fechaHoraLocal(fecha) {
  const desplazamiento = fecha.getTimezoneOffset() * 60_000;
  return new Date(fecha.getTime() - desplazamiento).toISOString().slice(0, 16);
}

function siguienteHorarioDisponible() {
  const siguiente = new Date();
  siguiente.setSeconds(0, 0);
  siguiente.setMinutes(Math.ceil((siguiente.getMinutes() + 1) / 30) * 30);
  return fechaHoraLocal(siguiente);
}

export default function AgendarCita() {
  const [datosGuardados] = useState(leerCitas);
  const [citas, setCitas] = useState(datosGuardados.citas);
  const [aviso, setAviso] = useState(datosGuardados.error);
  const [temaOscuro, setTemaOscuro] = useState(true);
  const [vistaPanel, setVistaPanel] = useState(false);
  const [servicioSeleccionado, setServicioSeleccionado] = useState('');
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [confirmacion, setConfirmacion] = useState('');
  const [fechaMinima, setFechaMinima] = useState(siguienteHorarioDisponible);

  useEffect(() => {
    try {
      localStorage.setItem(CLAVE_CITAS, JSON.stringify(citas));
    } catch (error) {
      setAviso(`No se pudo guardar la reserva en este navegador: ${error.message}`);
    }
  }, [citas]);

  useEffect(() => {
    if (!servicioSeleccionado) return;
    setFormulario((actual) => ({ ...actual, servicioId: servicioSeleccionado }));
    document.getElementById('agendar')?.scrollIntoView({ behavior: 'smooth' });
  }, [servicioSeleccionado]);

  function actualizar(evento) {
    setFormulario((actual) => ({ ...actual, [evento.target.name]: evento.target.value }));
    setConfirmacion('');
  }

  function cambiarEstado(id, estado) {
    setCitas((actuales) => actuales.map((cita) => (cita.id === id ? { ...cita, estado } : cita)));
  }

  function reservar(evento) {
    evento.preventDefault();
    const servicio = SERVICIOS.find((item) => item.id === Number(formulario.servicioId));
    const inicio = new Date(formulario.fechaHora);

    if (!servicio || Number.isNaN(inicio.getTime())) {
      setAviso('Selecciona un servicio y una fecha válidos.');
      return;
    }

    if (inicio <= new Date()) {
      setAviso('La fecha de la cita debe ser posterior a la hora actual.');
      return;
    }

    const hora = inicio.getHours() * 60 + inicio.getMinutes();
    const dia = inicio.getDay();
    const apertura = dia === 0 ? 600 : 480;
    const cierre = dia === 0 ? 960 : dia === 6 ? 1080 : 1200;
    const fin = hora + servicio.duracion;
    const terminaElMismoDia = fin < 1440;

    if (hora < apertura || fin > cierre || !terminaElMismoDia) {
      setAviso('El horario es lun–vie de 8:00 a. m. a 8:00 p. m.; sábados hasta las 6:00 p. m.; domingos de 10:00 a. m. a 4:00 p. m.');
      return;
    }

    const reservaSuperpuesta = citas.some((cita) => {
      if (cita.barbero !== formulario.barbero || cita.estado === 'CANCELADA') return false;
      const inicioExistente = new Date(cita.fechaHora).getTime();
      const finExistente = inicioExistente + cita.duracion * 60_000;
      return inicio.getTime() < finExistente && inicio.getTime() + servicio.duracion * 60_000 > inicioExistente;
    });

    if (reservaSuperpuesta) {
      setAviso('Ese barbero ya tiene una cita en ese horario. Elige otra hora.');
      return;
    }

    const cita = {
      ...formulario,
      id: globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`,
      servicioId: servicio.id,
      servicio: servicio.nombre,
      precio: servicio.precio,
      duracion: servicio.duracion,
      estado: 'CONFIRMADA',
    };

    setCitas((actuales) => [cita, ...actuales]);
    setFormulario(FORMULARIO_VACIO);
    setServicioSeleccionado('');
    setFechaMinima(siguienteHorarioDisponible());
    setAviso('');
    setConfirmacion(`¡Listo, ${cita.cliente}! Guardamos tu cita de ${cita.servicio} en este navegador.`);
  }

  function alternarVista() {
    setVistaPanel((actual) => !actual);
    setAviso('');
  }

  return (
    <div className={`app-shell ${temaOscuro ? 'theme-dark' : 'theme-light'}`}>
      <header className="site-header">
        <a className="brand" href="#inicio" onClick={() => setVistaPanel(false)} aria-label="El Caballero, inicio">
          <span className="brand-mark" aria-hidden="true">C.</span>
          <span><strong>EL CABALLERO</strong><small>BARBERÍA · ARMENIA</small></span>
        </a>
        <nav className="main-nav" aria-label="Navegación principal">
          {!vistaPanel && <>
            <a href="#servicios">Servicios</a><a href="#resenas">Reseñas</a><a href="#pagos">Medios de pago</a>
            <a className="nav-book" href="#agendar">Reservar <span aria-hidden="true">↗</span></a>
          </>}
          <button className="theme-toggle" type="button" onClick={() => setTemaOscuro((actual) => !actual)} aria-label={temaOscuro ? 'Activar modo claro' : 'Activar modo oscuro'}>
            <span aria-hidden="true">{temaOscuro ? '☼' : '☾'}</span> {temaOscuro ? 'Modo claro' : 'Modo oscuro'}
          </button>
          <button className="staff-link" type="button" onClick={alternarVista}>{vistaPanel ? '← Volver al sitio' : 'Panel barbero'}</button>
        </nav>
      </header>

      {vistaPanel ? <PanelBarbero citas={citas} actualizarEstado={cambiarEstado} /> : (
        <>
          {aviso && <div className="connection-notice" role="alert"><span>{aviso}</span><button type="button" aria-label="Cerrar aviso" onClick={() => setAviso('')}>×</button></div>}
          <section className="hero" id="inicio">
            <div className="hero-image" aria-hidden="true" />
            <div className="hero-content">
              <span className="eyebrow"><span className="eyebrow-dot" /> DESDE 2024 · ARMENIA, QUINDÍO</span>
              <h1>El arte de un<br />buen <span className="serif-accent">corte.</span></h1>
              <p>Buena conversación, un corte preciso y ese momento para ti que siempre hace falta.</p>
              <a className="button button-primary" href="#agendar">Encuentra tu momento <span aria-hidden="true">↗</span></a>
              <div className="hero-note"><span>01</span> Cuidamos cada detalle.</div>
            </div>
            <div className="hero-side-label">EL ESTILO SE SIENTE</div>
            <div className="hero-caption">CLÁSICOS DE SIEMPRE. ACTITUD DE HOY.</div>
          </section>

          <Servicios servicios={SERVICIOS} alReservar={setServicioSeleccionado} />
          <Reseña />
          <MedioPago />

          <section className="section booking-section" id="agendar">
            <div className="container booking-layout">
              <div className="booking-copy">
                <span className="eyebrow">Tu próxima visita empieza aquí</span>
                <h2>Un buen corte<br /><span className="serif-accent">te queda cerca.</span></h2>
                <p>Elige tu servicio, tu barbero y el horario que mejor te venga. Te esperamos.</p>
                <div className="booking-detail"><span>⌖</span> Armenia, Quindío · Colombia</div>
                <div className="booking-detail"><span>◷</span> Lun–vie · 8:00 a. m.–8:00 p. m. · Sáb · 8:00 a. m.–6:00 p. m. · Dom · 10:00 a. m.–4:00 p. m.</div>
              </div>
              <form className="booking-form" onSubmit={reservar}>
                <div className="form-heading"><span className="eyebrow">Reserva en un momento</span><h3>Agenda tu cita</h3></div>
                <label>Tu nombre<input name="cliente" autoComplete="name" placeholder="¿Cómo te llamas?" value={formulario.cliente} onChange={actualizar} required maxLength={100} /></label>
                <div className="form-row">
                  <label>Teléfono<input name="telefono" type="tel" autoComplete="tel" placeholder="+57 300 000 0000" value={formulario.telefono} onChange={actualizar} required maxLength={20} /></label>
                  <label>Correo electrónico<input name="correo" type="email" autoComplete="email" placeholder="tu@correo.com" value={formulario.correo} onChange={actualizar} required maxLength={150} /></label>
                </div>
                <label>Elige tu servicio
                  <select name="servicioId" value={formulario.servicioId} onChange={actualizar} required>
                    <option value="">Selecciona un servicio</option>
                    {SERVICIOS.map((servicio) => <option key={servicio.id} value={servicio.id}>{servicio.nombre} · {COP.format(servicio.precio)}</option>)}
                  </select>
                </label>
                <div className="form-row">
                  <label>Tu barbero
                    <select name="barbero" value={formulario.barbero} onChange={actualizar} required>
                      <option value="">Elige tu barbero</option>
                      {BARBEROS.map((barbero) => <option key={barbero} value={barbero}>{barbero}</option>)}
                    </select>
                  </label>
                  <label>Fecha y hora<input name="fechaHora" type="datetime-local" min={fechaMinima} step={1800} value={formulario.fechaHora} onChange={actualizar} required /></label>
                </div>
                <fieldset className="payment-options">
                  <legend>¿Cómo prefieres pagar?</legend>
                  {METODOS_PAGO.map((metodo) => (
                    <label className={`payment-option ${formulario.metodoPago === metodo.id ? 'selected' : ''}`} key={metodo.id}>
                      <input type="radio" name="metodoPago" value={metodo.id} checked={formulario.metodoPago === metodo.id} onChange={actualizar} required />
                      {metodo.nombre}
                    </label>
                  ))}
                </fieldset>
                <button className="button button-primary form-submit" type="submit">
                  Confirmar mi cita <span aria-hidden="true">↗</span>
                </button>
                {confirmacion && <p className="form-success" role="status">{confirmacion}</p>}
                <p className="form-footnote">Tus reservas se guardan en este navegador; no se envían a un servidor.</p>
              </form>
            </div>
          </section>

          <footer className="site-footer">
            <a className="brand" href="#inicio"><span className="brand-mark" aria-hidden="true">C.</span><span><strong>EL CABALLERO</strong><small>BARBERÍA · ARMENIA</small></span></a>
            <span>CALLE 93 · ARMENIA, QUINDÍO</span>
            <span>© 2026 · HECHO CON OFICIO</span>
          </footer>
        </>
      )}
    </div>
  );
}
