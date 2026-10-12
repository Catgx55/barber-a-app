import { COP } from '../data.js';

export default function PanelBarbero({ citas, actualizarEstado }) {
  const estados = ['CONFIRMADA', 'EN_CURSO', 'FINALIZADA', 'CANCELADA'];

  return (
    <section className="staff-page">
      <div className="container staff-container">
        <span className="eyebrow">Herramientas del equipo</span>
        <div className="staff-heading">
          <div><h1>Panel del barbero</h1><p>Consulta las reservas guardadas en este navegador y actualiza su estado.</p></div>
          <span className="staff-count">{citas.length}<small> reservas</small></span>
        </div>
        {citas.length === 0 ? <p className="staff-empty">Todavía no hay citas registradas.</p> : (
          <div className="appointments-table">
            <div className="appointment-header"><span>Cliente</span><span>Servicio y barbero</span><span>Fecha y hora</span><span>Pago</span><span>Estado</span></div>
            {citas.map((cita) => (
              <article className="appointment-row" key={cita.id}>
                <div className="client-cell"><strong>{cita.cliente}</strong><small>{cita.telefono}</small></div>
                <div><strong>{cita.servicio}</strong><small>{cita.barbero} · {COP.format(cita.precio)}</small></div>
                <span>{new Date(cita.fechaHora).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })}</span>
                <span>{cita.metodoPago}</span>
                <select
                  aria-label={`Estado de la cita de ${cita.cliente}`}
                  value={cita.estado}
                  onChange={(evento) => actualizarEstado(cita.id, evento.target.value)}
                >
                  {estados.map((estado) => <option key={estado} value={estado}>{estado.replace('_', ' ')}</option>)}
                </select>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
