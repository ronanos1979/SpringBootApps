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
    const message = typeof data === 'string' ? data : data.message || data.error || response.statusText;
    throw new Error(message);
  }

  return data;
}

function jsonRequest(path, method, body) {
  return request(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });
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

export function listAuthors() {
  return request('/api/authors');
}

export function searchAuthors(q) {
  return request(`/api/authors/search?q=${encodeURIComponent(q)}`);
}

export function getAuthor(id) {
  return request(`/api/authors/${id}`);
}

export function createAuthor(author) {
  return jsonRequest('/api/authors', 'POST', author);
}

export function updateAuthor(id, author) {
  return jsonRequest(`/api/authors/${id}`, 'PUT', author);
}

export function deleteAuthor(id) {
  return request(`/api/authors/${id}`, {
    method: 'DELETE',
  });
}

export function listBooks(mine = false) {
  return request(`/api/books${mine ? '?mine=true' : ''}`);
}

export function searchBooks(q) {
  return request(`/api/books/search?q=${encodeURIComponent(q)}`);
}

export function saveBookToCollection(bookId) {
  return request(`/api/books/${bookId}/save`, { method: 'POST' });
}

export function getBook(id) {
  return request(`/api/books/${id}`);
}

export function createBook(book) {
  return jsonRequest('/api/books', 'POST', book);
}

export function updateBook(id, book) {
  return jsonRequest(`/api/books/${id}`, 'PUT', book);
}

export function deleteBook(id) {
  return request(`/api/books/${id}`, {
    method: 'DELETE',
  });
}

export function getBookWords(bookId, mine = true) {
  return request(`/api/books/${bookId}/words?mine=${mine}`);
}

export function addWordToBook(bookId, word) {
  return jsonRequest(`/api/books/${bookId}/words`, 'POST', word);
}

export function bulkAddWordsToBook(bookId, payload) {
  return jsonRequest(`/api/books/${bookId}/words/bulk`, 'POST', payload);
}

export function removeWordFromBook(bookId, bookWordId) {
  return request(`/api/books/${bookId}/words/${bookWordId}`, {
    method: 'DELETE',
  });
}

export function listWords() {
  return request('/api/words');
}

export function searchWords(q) {
  return request(`/api/words/search?q=${encodeURIComponent(q)}`);
}

export function getWord(id) {
  return request(`/api/words/${id}`);
}

export function createWord(word) {
  return jsonRequest('/api/words', 'POST', word);
}

export function updateWord(id, word) {
  return jsonRequest(`/api/words/${id}`, 'PUT', word);
}

export function deleteWord(id) {
  return request(`/api/words/${id}`, {
    method: 'DELETE',
  });
}

export function listDefinitions() {
  return request('/api/definitions');
}

export function deleteDefinition(id) {
  return request(`/api/definitions/${id}`, {
    method: 'DELETE',
  });
}

export function listUsers() {
  return request('/api/users');
}

export function getUser(id) {
  return request(`/api/users/${id}`);
}

export function createUser(user) {
  return jsonRequest('/api/users', 'POST', user);
}

export function updateUser(id, user) {
  return jsonRequest(`/api/users/${id}`, 'PUT', user);
}

export function deleteUser(id) {
  return request(`/api/users/${id}`, {
    method: 'DELETE',
  });
}
