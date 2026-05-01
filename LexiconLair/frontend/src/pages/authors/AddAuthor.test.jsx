import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AddAuthor from './AddAuthor';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('AddAuthor', () => {
  it('posts a new author and navigates back to authors', async () => {
    const fetchMock = mockFetchSequence(jsonResponse({ id: 1, firstName: 'Toni', lastName: 'Morrison' }, {
      status: 201,
    }));

    renderWithAuth(<AddAuthor />, {
      initialEntries: ['/authors/add'],
      path: '/authors/add',
      routeAfterSubmit: '/authors',
    });

    await userEvent.type(screen.getByLabelText(/first name/i), 'Toni');
    await userEvent.type(screen.getByLabelText(/last name/i), 'Morrison');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('/authors route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledWith('/api/authors', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ firstName: 'Toni', lastName: 'Morrison' }),
    }));
  });

  it('loads an existing author and sends an update', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({ id: 1, firstName: 'Old', lastName: 'Name' }),
      jsonResponse({ id: 1, firstName: 'New', lastName: 'Name' }),
    );

    renderWithAuth(<AddAuthor />, {
      initialEntries: ['/authors/update/1'],
      path: '/authors/update/:id',
      routeAfterSubmit: '/authors',
    });

    const firstName = await screen.findByLabelText(/first name/i);
    await userEvent.clear(firstName);
    await userEvent.type(firstName, 'New');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('/authors route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors/1', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/authors/1', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ firstName: 'New', lastName: 'Name' }),
    }));
  });

  it('shows backend save errors', async () => {
    mockFetchSequence(jsonResponse({ message: 'Author exists' }, {
      ok: false,
      status: 409,
      statusText: 'Conflict',
    }));

    renderWithAuth(<AddAuthor />, {
      initialEntries: ['/authors/add'],
      path: '/authors/add',
    });

    await userEvent.type(screen.getByLabelText(/first name/i), 'Toni');
    await userEvent.type(screen.getByLabelText(/last name/i), 'Morrison');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    expect(await screen.findByText('Author exists')).toBeInTheDocument();
  });
});
