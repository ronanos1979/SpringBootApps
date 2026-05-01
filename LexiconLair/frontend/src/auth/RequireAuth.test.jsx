import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import RequireAuth from './RequireAuth';
import { AuthContext } from './authState';

function renderGuard(value, initialEntries = ['/words']) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <AuthContext.Provider value={value}>
        <Routes>
          <Route path="/login" element={<div>Login route</div>} />
          <Route path="/words" element={<RequireAuth><div>Protected route</div></RequireAuth>} />
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('RequireAuth', () => {
  it('shows a session check while auth state is loading', () => {
    renderGuard({ user: null, loading: true });

    expect(screen.getByText('Checking session...')).toBeInTheDocument();
  });

  it('redirects anonymous users to login', () => {
    renderGuard({ user: null, loading: false });

    expect(screen.getByText('Login route')).toBeInTheDocument();
  });

  it('renders children for authenticated users', () => {
    renderGuard({ user: { username: 'admin' }, loading: false });

    expect(screen.getByText('Protected route')).toBeInTheDocument();
  });
});
