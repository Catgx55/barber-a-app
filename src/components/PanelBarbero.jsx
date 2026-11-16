import { useEffect, useState } from 'react';
import { apiRequest, basicAuthorization } from '../api.js';
import { COP } from '../data.js';

export default function PanelBarbero() {
  const [credentials, setCredentials] = useState(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [user, setUser] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [statuses, setStatuses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [updatingId, setUpdatingId] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!credentials) return undefined;

    const controller = new AbortController();
    setLoading(true);
    Promise.all([
      apiRequest('/appointments', { authorization: credentials, signal: controller.signal }),
      apiRequest('/appointment-statuses', { authorization: credentials, signal: controller.signal }),
    ])
      .then(([appointmentList, statusList]) => {
        setAppointments(appointmentList);
        setStatuses(statusList);
        setError('');
      })
      .catch((requestError) => {
        if (requestError.name !== 'AbortError') {
          setError(requestError.message);
          if (requestError.message.startsWith('Credenciales')) {
            setCredentials(null);
            setUser(null);
          }
        }
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
  }, [credentials]);

  async function iniciarSesion(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    const authorization = basicAuthorization(email, password);
    try {
      const authenticatedUser = await apiRequest('/auth/me', { authorization });
      setCredentials(authorization);
      setUser(authenticatedUser);
      setPassword('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function actualizarEstado(appointment, statusId) {
    setUpdatingId(appointment.id);
    setError('');
    try {
      const updatedAppointment = await apiRequest(`/appointments/${appointment.id}/status`, {
        method: 'PATCH',
        body: { statusId: Number(statusId) },
        authorization: credentials,
      });
      setAppointments((current) => current.map((item) => (
        item.id === updatedAppointment.id ? updatedAppointment : item
      )));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setUpdatingId(null);
    }
  }

  function cerrarSesion() {
    setCredentials(null);
    setUser(null);
    setAppointments([]);
    setStatuses([]);
    setError('');
  }

  return (
    <section className="staff-page">
      <div className="container staff-container">
        <span className="eyebrow">Herramientas del equipo</span>
        {!credentials ? (
          <div className="staff-login">
            <div className="staff-heading">
              <div><h1>Panel del barbero</h1><p>Inicia sesión con una cuenta activa de barbero o administrador.</p></div>
            </div>
            {error && <p className="error-banner" role="alert">{error}</p>}
            <form className="booking-form" onSubmit={iniciarSesion}>
              <label>Correo electrónico
                <input
                  type="email"
                  autoComplete="username"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </label>
              <label>Contraseña
                <input
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                />
              </label>
              <button className="button button-primary form-submit" type="submit" disabled={loading}>
                {loading ? 'Verificando…' : 'Iniciar sesión'}
              </button>
            </form>
          </div>
        ) : (
          <>
            <div className="staff-heading">
              <div><h1>Panel del barbero</h1><p>Reservas guardadas en el servidor · Sesión: {user?.email}</p></div>
              <div className="staff-panel-actions">
                <span className="staff-count">{appointments.length}<small> reservas</small></span>
                <button className="staff-link" type="button" onClick={cerrarSesion}>Cerrar sesión</button>
              </div>
            </div>
            {error && <p className="error-banner" role="alert">{error}</p>}
            {loading ? <p className="staff-empty" role="status">Cargando agenda…</p> : appointments.length === 0 ? (
              <p className="staff-empty">Todavía no hay citas registradas.</p>
            ) : (
              <div className="appointments-table">
                <div className="appointment-header"><span>Cliente</span><span>Servicio y barbero</span><span>Fecha y hora</span><span>Pago</span><span>Estado</span></div>
                {appointments.map((appointment) => (
                  <article className="appointment-row" key={appointment.id}>
                    <div className="client-cell">
                      <strong>{appointment.clientName}</strong>
                      <small>{appointment.clientPhone} · #{appointment.id}</small>
                    </div>
                    <div>
                      <strong>{appointment.details.map((detail) => detail.serviceName).join(', ')}</strong>
                      <small>{appointment.barberName} · {COP.format(Number(appointment.totalAmount))}</small>
                    </div>
                    <span>{new Date(`${appointment.appointmentDate}T${appointment.startTime}`).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })}</span>
                    <span>{appointment.paymentMethod || 'No indicado'}</span>
                    <select
                      aria-label={`Estado de la cita de ${appointment.clientName}`}
                      value={appointment.statusId}
                      disabled={updatingId === appointment.id}
                      onChange={(event) => actualizarEstado(appointment, event.target.value)}
                    >
                      {statuses.map((status) => (
                        <option key={status.id} value={status.id}>{status.name.replace('_', ' ')}</option>
                      ))}
                    </select>
                  </article>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </section>
  );
}
