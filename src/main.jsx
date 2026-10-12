import React from 'react';
import { createRoot } from 'react-dom/client';
import AgendarCita from './components/AgendarCita.jsx';
import './styles.css';

// AgendarCita es la vista principal y coordina los otros cuatro componentes.
createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AgendarCita />
  </React.StrictMode>,
);
