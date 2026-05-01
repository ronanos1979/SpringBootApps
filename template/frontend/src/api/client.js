const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    ...options,
    headers: {
      ...options.headers,
    },
  });

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') ?? '';
  const data = contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof data === 'string' ? data : data.message || response.statusText;
    throw new Error(message);
  }

  return data;
}

export function getCurrentUser() {
  return request('/api/auth/me');
}

export function login(username, password) {
  const body = new URLSearchParams({ username, password });

  return request('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body,
  });
}

export function logout() {
  return request('/api/auth/logout', {
    method: 'POST',
  });
}

export function listUsers() {
  return request('/api/users');
}

export function createUser(user) {
  return request('/api/users', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(user),
  });
}

export function updateUser(id, user) {
  return request(`/api/users/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(user),
  });
}

export function deleteUser(id) {
  return request(`/api/users/${id}`, {
    method: 'DELETE',
  });
}
