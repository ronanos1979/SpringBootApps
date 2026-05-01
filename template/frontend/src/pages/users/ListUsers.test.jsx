import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { AuthContext } from '../../auth/authState';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import ListUsers from './ListUsers';

const users = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    firstName: 'Admin',
    lastName: 'User',
    createdAt: '2026-04-30T19:00:00',
    createdBy: -1,
    updatedAt: null,
    updatedBy: null,
  },
];

function renderListUsers() {
  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
        <ListUsers />
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('ListUsers', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('loads users from the backend', async () => {
    const fetchMock = mockFetchSequence(jsonResponse(users));

    renderListUsers();

    expect(await screen.findByText('admin@example.com')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith('/api/users', expect.objectContaining({ credentials: 'include' }));
  });

  it('deletes a user through the backend and removes it from the table', async () => {
    const user = userEvent.setup();
    const fetchMock = mockFetchSequence(jsonResponse(users), emptyResponse({ status: 204 }));

    renderListUsers();

    expect(await screen.findByText('admin')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /delete/i }));

    expect(fetchMock).toHaveBeenCalledWith('/api/users/1', expect.objectContaining({ method: 'DELETE' }));
    expect(await screen.findByText('No users found.')).toBeInTheDocument();
  });
});
