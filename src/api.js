const API_ROOT = '/api';

export function basicAuthorization(email, password) {
  const bytes = new TextEncoder().encode(`${email.trim()}:${password}`);
  let binary = '';
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return `Basic ${btoa(binary)}`;
}

export async function apiRequest(path, { method = 'GET', body, authorization, signal } = {}) {
  const headers = new Headers();
  if (body !== undefined) headers.set('Content-Type', 'application/json');
  if (authorization) headers.set('Authorization', authorization);

  let response;
  try {
    response = await fetch(`${API_ROOT}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new Error('No se pudo conectar con la API. Comprueba que Spring Boot esté ejecutándose.');
  }

  if (response.status === 204) return null;
  const contentType = response.headers.get('content-type') ?? '';
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message = typeof payload === 'object' && payload !== null
      ? payload.message ?? payload.error ?? 'La solicitud no se pudo completar.'
      : payload || 'La solicitud no se pudo completar.';
    throw new Error(response.status === 401 ? 'Credenciales inválidas o sesión no autorizada.' : message);
  }

  return payload;
}
