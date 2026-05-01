import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthContext } from '../../auth/authState';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import AddUser from './AddUser';

const existingUsers = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    firstName: 'Admin',
    lastName: 'User',
  },
];

function renderWithRoutes(initialPath) {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
        <Routes>
          <Route path="/users/add" element={<AddUser />} />
          <Route path="/users/update/:id" element={<AddUser />} />
          <Route path="/users" element={<div>Users page</div>} />
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('AddUser', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('creates a user through the backend', async () => {
    const user = userEvent.setup();
    const fetchMock = mockFetchSequence(jsonResponse({ id: 2 }));

    renderWithRoutes('/users/add');

    await user.type(screen.getByLabelText(/username/i), 'testuser');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/first name/i), 'Test');
    await user.type(screen.getByLabelText(/last name/i), 'User');
    await user.click(screen.getByRole('button', { name: /submit/i }));

    expect(fetchMock).toHaveBeenCalledWith('/api/users', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({
        username: 'testuser',
        password: 'password123',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
      }),
    }));
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({
      username: 'testuser',
      password: 'password123',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    });
    expect(await screen.findByText('Users page')).toBeInTheDocument();
  });

  it('loads and updates an existing user through the backend', async () => {
    const user = userEvent.setup();
    const fetchMock = mockFetchSequence(jsonResponse(existingUsers), emptyResponse());

    renderWithRoutes('/users/update/1');

    expect(await screen.findByDisplayValue('admin')).toBeInTheDocument();
    await user.clear(screen.getByLabelText(/first name/i));
    await user.type(screen.getByLabelText(/first name/i), 'Root');
    await user.click(screen.getByRole('button', { name: /submit/i }));

    expect(fetchMock).toHaveBeenCalledWith('/api/users/1', expect.objectContaining({
      method: 'PUT',
    }));
    expect(JSON.parse(fetchMock.mock.calls[1][1].body)).toEqual({
      username: 'admin',
      password: '',
      email: 'admin@example.com',
      firstName: 'Root',
      lastName: 'User',
    });
  });
});
