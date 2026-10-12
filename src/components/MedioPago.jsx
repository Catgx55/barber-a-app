import { METODOS_PAGO } from '../data.js';

export default function MedioPago() {
  return (
    <section className="section section-alt" id="pagos">
      <div className="container payment-layout">
        <div className="section-heading">
          <span className="eyebrow">Simple y sin sorpresas</span>
          <h2>Paga como<br /><span className="serif-accent">te quede mejor.</span></h2>
          <p>Reserva en línea y elige tu forma favorita de pagar cuando nos visites.</p>
        </div>
        <div className="payment-list">
          {METODOS_PAGO.map((metodo) => (
            <article className="payment-row" key={metodo.id}>
              <span className="payment-icon" aria-hidden="true">{metodo.icono}</span>
              <span><strong>{metodo.nombre}</strong><small>{metodo.detalle}</small></span>
              <span className="payment-check" aria-hidden="true">✓</span>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
