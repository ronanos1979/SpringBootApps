import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AddUser from './AddUser';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('AddUser', () => {
  it('posts a new user and navigates back to users', async () => {
    const fetchMock = mockFetchSequence(jsonResponse({ id: 3, username: 'reader' }, {
      status: 201,
    }));

    renderWithAuth(<AddUser />, {
      initialEntries: ['/users/add'],
      path: '/users/add',
      routeAfterSubmit: '/users',
    });

    await userEvent.type(screen.getByLabelText(/username/i), 'reader');
    await userEvent.type(screen.getByLabelText(/password/i), 'secret');
    await userEvent.type(screen.getByLabelText(/email/i), 'reader@example.com');
    await userEvent.type(screen.getByLabelText(/first name/i), 'Read');
    await userEvent.type(screen.getByLabelText(/last name/i), 'Er');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('/users route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledWith('/api/users', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({
        username: 'reader',
        password: 'secret',
        email: 'reader@example.com',
        firstName: 'Read',
        lastName: 'Er',
      }),
    }));
  });

  it('loads an existing user and sends an update without requiring password', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({
        id: 3,
        username: 'reader',
        email: 'reader@example.com',
        firstName: 'Read',
        lastName: 'Er',
      }),
      jsonResponse({ id: 3, username: 'reader2' }),
    );

    renderWithAuth(<AddUser />, {
      initialEntries: ['/users/update/3'],
      path: '/users/update/:id',
      routeAfterSubmit: '/users',
    });

    const username = await screen.findByLabelText(/username/i);
    await userEvent.clear(username);
    await userEvent.type(username, 'reader2');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('/users route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/users/3', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/users/3', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({
        username: 'reader2',
        password: '',
        email: 'reader@example.com',
        firstName: 'Read',
        lastName: 'Er',
      }),
    }));
  });
});
