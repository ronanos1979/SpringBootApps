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
        <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout }}>
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
});
