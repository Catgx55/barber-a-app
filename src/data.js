export const SERVICIOS = [
  { id: 1, nombre: 'Fade clásico', descripcion: 'Degradado a navaja, precisión en cada detalle.', precio: 25000, duracion: 45, imagen: 'https://images.unsplash.com/photo-1599351431202-1e0f0137899a?w=800&h=520&fit=crop&auto=format' },
  { id: 2, nombre: 'Corte tradicional', descripcion: 'El corte de siempre: tijera, peine y mucho estilo.', precio: 20000, duracion: 30, imagen: 'https://images.unsplash.com/photo-1568339434343-2a640a1a9946?w=800&h=520&fit=crop&auto=format' },
  { id: 3, nombre: 'Mullet moderno', descripcion: 'Un estilo retro reinventado y a tu medida.', precio: 30000, duracion: 60, imagen: 'https://images.unsplash.com/photo-1647140655214-e4a2d914971f?w=800&h=520&fit=crop&auto=format' },
  { id: 4, nombre: 'Degradado + diseño', descripcion: 'Degradado con diseño personalizado a navaja.', precio: 35000, duracion: 75, imagen: 'https://images.unsplash.com/photo-1630827020718-3433092696e7?w=800&h=520&fit=crop&auto=format' },
  { id: 5, nombre: 'Afeitado con navaja', descripcion: 'Toalla caliente y un afeitado clásico y relajante.', precio: 18000, duracion: 35, imagen: 'https://images.unsplash.com/photo-1621645582931-d1d3e6564943?w=800&h=520&fit=crop&auto=format' },
  { id: 6, nombre: 'Corte + perfilado', descripcion: 'Corte completo con perfilado de barba y contornos.', precio: 28000, duracion: 50, imagen: 'https://images.unsplash.com/photo-1629189784191-9afdcbcb0398?w=800&h=520&fit=crop&auto=format' },
];

export const BARBEROS = ['Carlos Mendoza', 'Diego Ríos', 'Andrés Vargas'];

export const METODOS_PAGO = [
  { id: 'NEQUI', nombre: 'Nequi', icono: '◉', detalle: 'Transferencia o código QR' },
  { id: 'DAVIPLATA', nombre: 'Daviplata', icono: '◈', detalle: 'Transferencia o código QR' },
  { id: 'EFECTIVO', nombre: 'Efectivo', icono: '＄', detalle: 'Paga en el local al finalizar' },
];

export const COP = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

export const CLAVE_CITAS = 'el-caballero-citas';
