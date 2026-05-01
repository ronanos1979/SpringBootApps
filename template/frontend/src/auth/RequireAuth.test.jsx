import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import RequireAuth from './RequireAuth';
import { jsonResponse } from '../test/fetchMock';

function renderProtectedRoute(initialPath = '/definitions') {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>Login page</div>} />
          <Route
            path="/definitions"
            element={(
              <RequireAuth>
                <div>Definitions page</div>
              </RequireAuth>
            )}
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe('RequireAuth', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders protected content when a session exists', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ username: 'admin' }));

    renderProtectedRoute();

    expect(await screen.findByText('Definitions page')).toBeInTheDocument();
  });

  it('redirects to login when no session exists', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse({ message: 'Unauthorized' }, {
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
    }));

    renderProtectedRoute();

    await waitFor(() => expect(screen.getByText('Login page')).toBeInTheDocument());
  });
});
