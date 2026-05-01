import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import AddBook from './AddBook';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { AuthContext } from '../../auth/authState';

afterEach(() => {
  vi.restoreAllMocks();
});

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/books/add']}>
      <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
        <Routes>
          <Route path="/books/add" element={<AddBook />} />
          <Route path="/books" element={<div>Books route</div>} />
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('AddBook', () => {
  it('loads authors and posts a new book', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{ id: 7, firstName: 'Mary', lastName: 'Shelley', displayName: 'Mary Shelley' }]),
      jsonResponse({ id: 2, title: 'Frankenstein' }, { status: 201 }),
    );

    renderPage();

    await userEvent.type(await screen.findByLabelText(/title/i), 'Frankenstein');
    await userEvent.selectOptions(screen.getByLabelText(/author/i), '7');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('Books route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/books', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ title: 'Frankenstein', authorId: 7 }),
    }));
  });

  it('loads an existing book and sends an update', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{ id: 7, firstName: 'Mary', lastName: 'Shelley', displayName: 'Mary Shelley' }]),
      jsonResponse({ id: 2, title: 'Old Title', author: { id: 7, displayName: 'Mary Shelley' } }),
      jsonResponse({ id: 2, title: 'New Title', author: { id: 7, displayName: 'Mary Shelley' } }),
    );

    render(
      <MemoryRouter initialEntries={['/books/update/2']}>
        <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
          <Routes>
            <Route path="/books/update/:id" element={<AddBook />} />
            <Route path="/books" element={<div>Books route</div>} />
          </Routes>
        </AuthContext.Provider>
      </MemoryRouter>,
    );

    const title = await screen.findByLabelText(/title/i);
    await userEvent.clear(title);
    await userEvent.type(title, 'New Title');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('Books route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/authors', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/books/2', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/books/2', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ title: 'New Title', authorId: 7 }),
    }));
  });

  it('shows backend save errors', async () => {
    mockFetchSequence(
      jsonResponse([{ id: 7, firstName: 'Mary', lastName: 'Shelley', displayName: 'Mary Shelley' }]),
      jsonResponse({ message: 'Invalid author id' }, {
        ok: false,
        status: 400,
        statusText: 'Bad Request',
      }),
    );

    renderPage();

    await userEvent.type(await screen.findByLabelText(/title/i), 'Frankenstein');
    await userEvent.selectOptions(screen.getByLabelText(/author/i), '7');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    expect(await screen.findByText('Invalid author id')).toBeInTheDocument();
  });
});
