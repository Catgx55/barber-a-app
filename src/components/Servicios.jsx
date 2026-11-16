import { COP } from '../data.js';

export default function Servicios({ servicios, alReservar }) {
  return (
    <section className="section section-alt" id="servicios">
      <div className="container">
        <div className="section-heading">
          <span className="eyebrow">El ritual, a tu manera</span>
          <h2>Servicios pensados para ti</h2>
          <p>Cortes con intención, buenos productos y el tiempo que mereces.</p>
        </div>
        {servicios.length === 0 ? <p className="catalog-state">No hay servicios activos disponibles por ahora.</p> : (
        <div className="services-grid">
          {servicios.map((servicio, indice) => (
            <article className="service-card" key={servicio.id}>
              <div className="service-image-frame">
                <img
                  className="service-image"
                  src={servicio.imagen}
                  alt={`${servicio.nombre}: estilo de corte en barbería`}
                  loading="lazy"
                  decoding="async"
                />
                <span className="service-number">0{indice + 1}</span>
              </div>
              <div className="service-content">
                <h3>{servicio.nombre}</h3>
                <p>{servicio.descripcion}</p>
                <div className="service-meta">
                  <span>{servicio.duracion} min</span>
                  <strong>{COP.format(servicio.precio)}</strong>
                </div>
                <button className="text-button" type="button" onClick={() => alReservar(servicio.id)}>
                  Elegir servicio <span aria-hidden="true">↗</span>
                </button>
              </div>
            </article>
          ))}
        </div>
        )}
      </div>
    </section>
  );
}
