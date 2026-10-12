export default function Reseña() {
  const resenas = [
    { nombre: 'Mateo García', iniciales: 'MG', servicio: 'Fade clásico', texto: 'El mejor fade que me han hecho. Carlos tiene una precisión increíble. ¡Ya volví tres veces!' },
    { nombre: 'Santiago López', iniciales: 'SL', servicio: 'Corte tradicional', texto: 'Un lugar muy cómodo y una atención de primera. Se nota el cuidado en cada detalle.' },
    { nombre: 'Alejandro Ruiz', iniciales: 'AR', servicio: 'Mullet moderno', texto: 'Llevé una referencia y ejecutaron el estilo tal cual. Muy detallistas y profesionales.' },
  ];

  return (
    <section className="section" id="resenas">
      <div className="container">
        <div className="reviews-intro">
          <div className="section-heading">
            <span className="eyebrow">Palabra de nuestros clientes</span>
            <h2>Buenos cortes.<br /><span className="serif-accent">Mejores historias.</span></h2>
          </div>
          <div className="rating-box"><strong>4.9<span> / 5</span></strong><span className="stars" aria-label="Cinco de cinco estrellas">★★★★★</span><span>Clientes que regresan.</span></div>
        </div>
        <div className="reviews-grid">
          {resenas.map((resena) => (
            <article className="review-card" key={resena.iniciales}>
              <span className="quote-mark" aria-hidden="true">“</span>
              <p>{resena.texto}</p>
              <div className="review-author">
                <span className="avatar">{resena.iniciales}</span>
                <span><strong>{resena.nombre}</strong><small>{resena.servicio}</small></span>
                <span className="verified" aria-label="Cliente verificado">✓</span>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
