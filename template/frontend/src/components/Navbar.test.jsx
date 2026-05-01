import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../auth/AuthContext';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../test/fetchMock';
import Navbar from './Navbar';

describe('Navbar', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows the current user and logs out through the backend', async () => {
    const user = userEvent.setup();
    const fetchMock = mockFetchSequence(
      jsonResponse({ username: 'admin' }),
      emptyResponse({ status: 204 }),
    );

    render(
      <MemoryRouter initialEntries={['/']}>
        <AuthProvider>
          <Routes>
            <Route path="/" element={<Navbar />} />
            <Route path="/login" element={<div>Login page</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );

    expect(await screen.findByText('Signed in as admin')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /logout/i }));

    expect(fetchMock).toHaveBeenCalledWith('/api/auth/logout', expect.objectContaining({ method: 'POST' }));
    expect(await screen.findByText('Login page')).toBeInTheDocument();
  });
});
