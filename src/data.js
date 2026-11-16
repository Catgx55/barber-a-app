export const IMAGENES_SERVICIOS = {
  'Fade clásico': 'https://images.unsplash.com/photo-1599351431202-1e0f0137899a?w=800&h=520&fit=crop&auto=format',
  'Corte tradicional': 'https://images.unsplash.com/photo-1568339434343-2a640a1a9946?w=800&h=520&fit=crop&auto=format',
  'Mullet moderno': 'https://images.unsplash.com/photo-1647140655214-e4a2d914971f?w=800&h=520&fit=crop&auto=format',
  'Degradado + diseño': 'https://images.unsplash.com/photo-1630827020718-3433092696e7?w=800&h=520&fit=crop&auto=format',
  'Afeitado con navaja': 'https://images.unsplash.com/photo-1621645582931-d1d3e6564943?w=800&h=520&fit=crop&auto=format',
  'Corte + perfilado': 'https://images.unsplash.com/photo-1629189784191-9afdcbcb0398?w=800&h=520&fit=crop&auto=format',
};

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
