import { render } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthContext } from '../auth/authState';

export function renderWithAuth(ui, options = {}) {
  const {
    path = '/',
    initialEntries = [path],
    routeAfterSubmit,
    user = { username: 'admin', firstName: 'Ada', role: 'ADMIN' },
    loading = false,
    logout = vi.fn(),
  } = options;

  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <AuthContext.Provider value={{ user, loading, logout }}>
        <Routes>
          <Route path={path} element={ui} />
          {routeAfterSubmit && <Route path={routeAfterSubmit} element={<div>{routeAfterSubmit} route</div>} />}
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}
