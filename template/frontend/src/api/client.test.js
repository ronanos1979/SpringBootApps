import {
  createUser,
  deleteUser,
  getCurrentUser,
  listUsers,
  login,
  logout,
  updateUser,
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

  it('calls user CRUD endpoints with the expected HTTP methods', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(await mockResponse({ id: 1 }));
    const user = {
      username: 'testuser',
      password: 'password123',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    };

    await listUsers();
    await createUser(user);
    await updateUser(1, user);
    await deleteUser(1);
    await logout();

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/users', expect.objectContaining({ credentials: 'include' }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/users', expect.objectContaining({
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/users/1', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify(user),
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/users/1', expect.objectContaining({ method: 'DELETE' }));
    expect(fetchMock).toHaveBeenNthCalledWith(5, '/api/auth/logout', expect.objectContaining({ method: 'POST' }));
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
