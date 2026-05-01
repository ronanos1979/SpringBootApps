import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../test/fetchMock';
import Login from './Login';

function renderLogin(initialPath = '/login') {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<div>Home page</div>} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('Login', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('submits credentials and navigates after successful login', async () => {
    const user = userEvent.setup();
    const fetchMock = mockFetchSequence(
      jsonResponse({ message: 'Unauthorized' }, { ok: false, status: 401, statusText: 'Unauthorized' }),
      emptyResponse(),
      jsonResponse({ username: 'admin' }),
    );

    renderLogin();

    await user.type(screen.getByLabelText(/username/i), 'admin');
    await user.type(screen.getByLabelText(/password/i), 'change-me');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({
      method: 'POST',
      body: expect.any(URLSearchParams),
    }));
    expect(fetchMock.mock.calls[1][1].body.toString()).toBe('username=admin&password=change-me');
    expect(await screen.findByText('Home page')).toBeInTheDocument();
  });

  it('shows an error when login fails', async () => {
    const user = userEvent.setup();
    mockFetchSequence(
      jsonResponse({ message: 'Unauthorized' }, { ok: false, status: 401, statusText: 'Unauthorized' }),
      jsonResponse({ message: 'Bad credentials' }, { ok: false, status: 401, statusText: 'Unauthorized' }),
    );

    renderLogin();

    await user.type(screen.getByLabelText(/username/i), 'admin');
    await user.type(screen.getByLabelText(/password/i), 'wrong');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Bad credentials')).toBeInTheDocument();
  });
});
