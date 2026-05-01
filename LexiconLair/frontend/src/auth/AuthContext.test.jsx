import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthProvider } from './AuthContext';
import { useAuth } from './useAuth';
import { jsonResponse, mockFetchSequence } from '../test/fetchMock';

afterEach(() => {
  vi.restoreAllMocks();
});

function Probe() {
  const { user, loading, login, logout } = useAuth();

  if (loading) {
    return <div>Loading auth</div>;
  }

  return (
    <div>
      <div>User: {user?.username ?? 'anonymous'}</div>
      <button onClick={() => login('admin', 'change-me')}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
}

describe('AuthProvider', () => {
  it('loads the current user on mount', async () => {
    mockFetchSequence(jsonResponse({ username: 'admin' }));

    render(<AuthProvider><Probe /></AuthProvider>);

    expect(await screen.findByText('User: admin')).toBeInTheDocument();
  });

  it('sets anonymous user when session check fails', async () => {
    mockFetchSequence(jsonResponse({ message: 'Unauthorized' }, {
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
    }));

    render(<AuthProvider><Probe /></AuthProvider>);

    expect(await screen.findByText('User: anonymous')).toBeInTheDocument();
  });

  it('logs in, refreshes current user, and logs out', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({ message: 'Unauthorized' }, { ok: false, status: 401 }),
      jsonResponse('', { contentType: 'text/plain' }),
      jsonResponse({ username: 'admin' }),
      jsonResponse('', { contentType: 'text/plain' }),
    );

    render(<AuthProvider><Probe /></AuthProvider>);

    expect(await screen.findByText('User: anonymous')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Login' }));
    expect(await screen.findByText('User: admin')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Logout' }));

    await waitFor(() => expect(screen.getByText('User: anonymous')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/auth/login', expect.objectContaining({
      method: 'POST',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/auth/logout', expect.objectContaining({
      method: 'POST',
    }));
  });
});
