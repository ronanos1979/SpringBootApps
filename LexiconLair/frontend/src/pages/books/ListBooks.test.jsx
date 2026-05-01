import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ListBooks from './ListBooks';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ListBooks', () => {
  it('loads books and deletes after backend success', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{ id: 4, title: 'Beloved', author: { displayName: 'Toni Morrison' } }]),
      emptyResponse({ status: 204 }),
    );

    renderWithAuth(<ListBooks />, { path: '/books' });

    expect(await screen.findByText('Beloved')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => expect(screen.queryByText('Beloved')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/books', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/books/4', expect.objectContaining({
      method: 'DELETE',
    }));
  });

  it('shows load errors', async () => {
    mockFetchSequence(jsonResponse({ message: 'No session' }, {
      ok: false,
      status: 401,
      statusText: 'Unauthorized',
    }));

    renderWithAuth(<ListBooks />, { path: '/books' });

    expect(await screen.findByText('No session')).toBeInTheDocument();
  });
});
