import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ListUsers from './ListUsers';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ListUsers', () => {
  it('loads users and deletes after backend success', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{
        id: 3,
        username: 'reader',
        email: 'reader@example.com',
        firstName: 'Read',
        lastName: 'Er',
      }]),
      emptyResponse({ status: 204 }),
    );

    renderWithAuth(<ListUsers />, { path: '/users' });

    expect(await screen.findByText('reader')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => expect(screen.queryByText('reader')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/users', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/users/3', expect.objectContaining({
      method: 'DELETE',
    }));
  });
});
