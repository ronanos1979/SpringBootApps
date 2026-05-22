import {
  bulkAddWordsToBook,
  createAuthor,
  createBook,
  createUser,
  createWord,
  deleteAuthor,
  deleteBook,
  deleteDefinition,
  deleteUser,
  deleteWord,
  getAuthor,
  getAdminSettings,
  getBook,
  getCurrentUser,
  getGameQuestion,
  getUser,
  getWord,
  listWordsWithoutDefinitions,
  listAuthors,
  listBooks,
  listDefinitions,
  listUsers,
  listWords,
  login,
  logout,
  refreshMissingWordDefinitions,
  refreshWordDefinitions,
  saveBookToCollection,
  searchAuthors,
  searchBooks,
  searchWords,
  updateAuthor,
  updateAdminSettings,
  updateBook,
  updateUser,
  updateWord,
} from './client';

afterEach(() => {
  vi.restoreAllMocks();
});

function mockResponse(body, init = {}) {
  return Promise.resolve({
    ok: init.ok ?? true,
    status: init.status ?? 200,
    statusText: init.statusText ?? 'OK',
    headers: {
      get: name => (name.toLowerCase() === 'content-type' ? init.contentType ?? 'application/json' : null),
    },
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(typeof body === 'string' ? body : JSON.stringify(body)),
  });
}

describe('api client', () => {
  it('logs in with form encoded credentials and includes cookies', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse('', {
      contentType: 'text/plain',
    }));

    await login('admin', 'change-me');

    expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: expect.any(URLSearchParams),
    }));
    expect(fetchMock.mock.calls[0][1].body.toString()).toBe('username=admin&password=change-me');
  });

  it('reads the current user', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse({ username: 'admin' }));

    await expect(getCurrentUser()).resolves.toEqual({ username: 'admin' });
    expect(globalThis.fetch).toHaveBeenCalledWith('/api/auth/me', expect.objectContaining({
      credentials: 'include',
    }));
  });

  it('calls list and delete endpoints with expected routes', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse([]));

    await listAuthors();
    await listBooks();
    await listWords();
    await listDefinitions();
    await listUsers();
    await deleteAuthor(1);
    await deleteBook(4);
    await deleteWord(8);
    await deleteDefinition(2);
    await deleteUser(3);
    await logout();

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/books', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/words', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/definitions', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/users', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/authors/1', expect.objectContaining({ method: 'DELETE' }));
    expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/books/4', expect.objectContaining({ method: 'DELETE' }));
    expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/words/8', expect.objectContaining({ method: 'DELETE' }));
    expect(fetchMock).toHaveBeenNthCalledWith(9, '/api/definitions/2', expect.objectContaining({ method: 'DELETE' }));
    expect(fetchMock).toHaveBeenNthCalledWith(10, '/api/users/3', expect.objectContaining({ method: 'DELETE' }));
    expect(fetchMock).toHaveBeenNthCalledWith(11, '/api/auth/logout', expect.objectContaining({ method: 'POST' }));
  });

  it('calls get endpoints with expected routes', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse({ id: 1 }));

    await getAuthor(1);
    await getAdminSettings();
    await getGameQuestion('easy');
    await getBook(2);
    await getWord(3);
    await getUser(4);

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors/1', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/admin/settings', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/game/question?mode=easy', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/books/2', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/words/3', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/users/4', expect.objectContaining({ credentials: 'include' }));
  });

  it('serializes create and update payloads as JSON', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse({ id: 1 }));

    await createAuthor({ firstName: 'Octavia', lastName: 'Butler' });
    await createBook({ title: 'Kindred', authorId: 7 });
    await createWord({ text: 'lexicon', language: 'English' });
    await createUser({ username: 'reader', password: 'secret', email: 'reader@example.com' });
    await updateAdminSettings({
      externalApiDelayMs: 100,
      externalApiBatchSize: 5,
      gameOptionCount: 6,
      gameQuestionCount: 12,
    });
    await updateAuthor(1, { firstName: 'Octavia', lastName: 'Butler' });
    await updateBook(1, { title: 'Parable', authorId: 7 });
    await updateWord(1, { text: 'parable', language: 'English' });
    await updateUser(1, { username: 'reader', password: '', email: 'reader@example.com' });

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors', expect.objectContaining({
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ firstName: 'Octavia', lastName: 'Butler' }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/books', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ title: 'Kindred', authorId: 7 }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/words', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ text: 'lexicon', language: 'English' }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/users', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ username: 'reader', password: 'secret', email: 'reader@example.com' }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/admin/settings', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({
        externalApiDelayMs: 100,
        externalApiBatchSize: 5,
        gameOptionCount: 6,
        gameQuestionCount: 12,
      }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/authors/1', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ firstName: 'Octavia', lastName: 'Butler' }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/books/1', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ title: 'Parable', authorId: 7 }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/words/1', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ text: 'parable', language: 'English' }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(9, '/api/users/1', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ username: 'reader', password: '', email: 'reader@example.com' }),
    }));
  });

  it('calls search and save-to-collection endpoints with correct URLs', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse([]));

    await searchBooks('hamlet');
    await searchAuthors('shake');
    await searchWords('stoic');
    await saveBookToCollection(5);
    await bulkAddWordsToBook(1, { words: ['ephemeral', 'serendipity'], language: 'en' });
    await listWordsWithoutDefinitions();
    await refreshWordDefinitions(8);
    await refreshMissingWordDefinitions();

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/books/search?q=hamlet', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/authors/search?q=shake', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/words/search?q=stoic', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/books/5/save', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/books/1/words/bulk', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ words: ['ephemeral', 'serendipity'], language: 'en' }),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/words/without-definitions', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/words/8/definitions/refresh', expect.objectContaining({ method: 'POST' }));
    expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/words/definitions/refresh-missing', expect.objectContaining({ method: 'POST' }));
  });

  it('throws backend error messages for failed responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse({ message: 'Bad credentials' }, {
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
    }));

    await expect(login('admin', 'wrong')).rejects.toThrow('Bad credentials');
  });
});
