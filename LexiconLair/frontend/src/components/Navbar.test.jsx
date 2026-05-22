import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import Navbar from './Navbar';
import { AuthContext } from '../auth/authState';

describe('Navbar', () => {
  it('logs out through auth context and navigates to login', async () => {
    const logout = vi.fn().mockResolvedValue();

    render(
      <MemoryRouter initialEntries={['/']}>
        <AuthContext.Provider value={{ user: { username: 'admin', role: 'ADMIN' }, loading: false, logout }}>
          <Routes>
            <Route path="/" element={<Navbar />} />
            <Route path="/login" element={<div>Login route</div>} />
          </Routes>
        </AuthContext.Provider>
      </MemoryRouter>,
    );

    await userEvent.click(screen.getByRole('button', { name: /logout/i }));

    await waitFor(() => expect(screen.getByText('Login route')).toBeInTheDocument());
    expect(logout).toHaveBeenCalled();
  });

  it('hides admin navigation for regular users', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <AuthContext.Provider value={{ user: { username: 'reader', role: 'USER' }, loading: false, logout: vi.fn() }}>
          <Navbar />
        </AuthContext.Provider>
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: /home/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /game/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /add book/i })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /^admin$/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /^users$/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /^definitions$/i })).not.toBeInTheDocument();
  });
});
