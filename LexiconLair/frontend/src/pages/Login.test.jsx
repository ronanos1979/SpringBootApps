import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Login from './Login';
import { AuthContext } from '../auth/authState';

function renderLogin(login = vi.fn().mockResolvedValue({ username: 'admin' }), initialEntries = ['/login']) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <AuthContext.Provider value={{ login, user: null, loading: false }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<div>Home route</div>} />
          <Route path="/words" element={<div>Words route</div>} />
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('Login', () => {
  it('submits credentials and navigates to the requested route', async () => {
    const login = vi.fn().mockResolvedValue({ username: 'admin' });

    renderLogin(login, ['/login']);

    await userEvent.type(screen.getByLabelText(/username/i), 'admin');
    await userEvent.type(screen.getByLabelText(/password/i), 'change-me');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(screen.getByText('Home route')).toBeInTheDocument());
    expect(login).toHaveBeenCalledWith('admin', 'change-me');
  });

  it('shows login failures', async () => {
    renderLogin(vi.fn().mockRejectedValue(new Error('Bad credentials')));

    await userEvent.type(screen.getByLabelText(/username/i), 'admin');
    await userEvent.type(screen.getByLabelText(/password/i), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Bad credentials')).toBeInTheDocument();
  });

  it('shows logout confirmation from query string', () => {
    renderLogin(vi.fn(), ['/login?logout']);

    expect(screen.getByText('You have been logged out.')).toBeInTheDocument();
  });
});
