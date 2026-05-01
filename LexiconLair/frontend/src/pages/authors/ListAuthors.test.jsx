import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import ListAuthors from './ListAuthors';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { AuthContext } from '../../auth/authState';

afterEach(() => {
  vi.restoreAllMocks();
});

function renderPage() {
  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
        <ListAuthors />
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('ListAuthors', () => {
  it('loads authors from the API and deletes after backend success', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{ id: 1, firstName: 'Jane', lastName: 'Austen', displayName: 'Jane Austen' }]),
      emptyResponse({ status: 204 }),
    );

    renderPage();

    expect(await screen.findByText('Jane')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => expect(screen.queryByText('Jane')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/authors/1', expect.objectContaining({
      method: 'DELETE',
    }));
  });
});
