import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import AddBook from './AddBook';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { AuthContext } from '../../auth/authState';

afterEach(() => {
  vi.restoreAllMocks();
});

const shelley = { id: 7, firstName: 'Mary', lastName: 'Shelley', displayName: 'Mary Shelley' };

function renderPage(extraRoutes = null) {
  return render(
    <MemoryRouter initialEntries={['/books/add']}>
      <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
        <Routes>
          <Route path="/books/add" element={<AddBook />} />
          <Route path="/books" element={<div>Books route</div>} />
          <Route path="/books/:id" element={<div>Book detail route</div>} />
          {extraRoutes}
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('AddBook', () => {
  it('loads authors and posts a new book', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([shelley]),
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
    expect(fetchMock).toHaveBeenCalledWith('/api/books', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ title: 'Frankenstein', authorId: 7 }),
    }));
  });

  it('loads an existing book and sends an update', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([shelley]),
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
      jsonResponse([shelley]),
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

  it('shows 409 error when book title already exists', async () => {
    mockFetchSequence(
      jsonResponse([shelley]),
      jsonResponse({ message: 'A book with this title already exists' }, { ok: false, status: 409 }),
    );

    renderPage();

    await userEvent.type(await screen.findByLabelText(/title/i), 'Frankenstein');
    await userEvent.selectOptions(screen.getByLabelText(/author/i), '7');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    expect(await screen.findByText('A book with this title already exists')).toBeInTheDocument();
  });

  it('shows live search suggestions as title is typed', async () => {
    const frankenstein = { id: 2, title: 'Frankenstein', author: shelley };
    mockFetchSequence(
      jsonResponse([shelley]),         // listAuthors on mount
      jsonResponse([frankenstein]),    // searchBooks after debounce
    );

    renderPage();
    await screen.findByLabelText(/title/i); // wait for load

    await userEvent.type(screen.getByLabelText(/title/i), 'Fr');

    // suggestions appear after 300ms debounce — findByText waits up to 1000ms
    expect(await screen.findByText('Frankenstein')).toBeInTheDocument();
    expect(screen.getByText(/\+ add to my list/i)).toBeInTheDocument();
    expect(screen.getByText('by Mary Shelley')).toBeInTheDocument();
  });

  it('saves existing book to collection and navigates to book detail on suggestion click', async () => {
    const frankenstein = { id: 2, title: 'Frankenstein', author: shelley };
    const fetchMock = mockFetchSequence(
      jsonResponse([shelley]),                                      // listAuthors
      jsonResponse([frankenstein]),                                 // searchBooks
      jsonResponse(frankenstein),                                   // saveBookToCollection
    );

    renderPage();
    await screen.findByLabelText(/title/i);

    await userEvent.type(screen.getByLabelText(/title/i), 'Fr');

    const suggestion = await screen.findByText('Frankenstein');
    await userEvent.click(suggestion.closest('li'));

    await waitFor(() => expect(screen.getByText('Book detail route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledWith('/api/books/2/save', expect.objectContaining({
      method: 'POST',
    }));
  });

  it('shows no suggestions hint when typing yields no matches', async () => {
    mockFetchSequence(
      jsonResponse([shelley]),   // listAuthors
      jsonResponse([]),          // searchBooks returns empty
    );

    renderPage();
    await screen.findByLabelText(/title/i);

    await userEvent.type(screen.getByLabelText(/title/i), 'Zz');

    await waitFor(() =>
      expect(screen.getByText(/no existing books found with this title/i)).toBeInTheDocument(),
    );
  });

  it('does not show suggestions in edit mode', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([shelley]),
      jsonResponse({ id: 2, title: 'Old', author: { id: 7, displayName: 'Mary Shelley' } }),
    );

    render(
      <MemoryRouter initialEntries={['/books/update/2']}>
        <AuthContext.Provider value={{ user: { username: 'admin' }, loading: false, logout: vi.fn() }}>
          <Routes>
            <Route path="/books/update/:id" element={<AddBook />} />
          </Routes>
        </AuthContext.Provider>
      </MemoryRouter>,
    );

    await userEvent.type(await screen.findByLabelText(/title/i), 'Fr');

    // search endpoint must NOT be called in edit mode
    expect(fetchMock).not.toHaveBeenCalledWith(
      expect.stringContaining('/api/books/search'),
      expect.anything(),
    );
  });
});
