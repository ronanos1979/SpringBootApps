import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import WordSearch from './WordSearch';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

const searchResult = {
  bookWordId: 5,
  bookId: 1,
  bookTitle: 'Hamlet',
  authorDisplayName: 'William Shakespeare',
  word: { id: 2, text: 'ephemeral', language: 'en' },
  definitions: [
    { id: 3, partOfSpeech: 'adjective', definitionText: 'Lasting a very short time.', example: 'An ephemeral joy.' },
  ],
  createdAt: '2026-05-22T10:00:00',
};

describe('WordSearch', () => {
  it('renders the search page heading and all-user scope description', () => {
    renderWithAuth(<WordSearch />);

    expect(screen.getByRole('heading', { name: /word search/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/search for a word/i)).toBeInTheDocument();
    expect(screen.getByText(/all words added to any book by any user/i)).toBeInTheDocument();
  });

  it('shows results from other users', async () => {
    const otherUserResult = {
      ...searchResult,
      bookWordId: 99,
      word: { id: 10, text: 'ephemeral', language: 'en' },
      createdAt: '2026-05-20T08:00:00',
    };
    mockFetchSequence(jsonResponse([searchResult, otherUserResult]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    // both results rendered — one per bookWordId key
    const cards = await screen.findAllByText('ephemeral');
    expect(cards.length).toBeGreaterThanOrEqual(2);
  });

  it('shows results after search', async () => {
    const fetchMock = mockFetchSequence(jsonResponse([searchResult]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText('ephemeral')).toBeInTheDocument();
    expect(screen.getByText('Lasting a very short time.')).toBeInTheDocument();
    expect(screen.getByText(/"An ephemeral joy\."/i)).toBeInTheDocument();

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/words/search?q=ephemeral',
      expect.objectContaining({ credentials: 'include' }),
    );
  });

  it('shows book context link in results', async () => {
    mockFetchSequence(jsonResponse([searchResult]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    await screen.findByText('ephemeral');

    const bookLink = screen.getByRole('link', { name: /hamlet/i });
    expect(bookLink).toHaveAttribute('href', '/books/1');
    expect(bookLink.textContent).toContain('William Shakespeare');
  });

  it('shows empty state when no results', async () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'zzzunknown');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText(/no words found matching/i)).toBeInTheDocument();
  });

  it('does not show empty state before first search', () => {
    renderWithAuth(<WordSearch />);

    expect(screen.queryByText(/no words found/i)).not.toBeInTheDocument();
  });

  it('shows error when search fails', async () => {
    mockFetchSequence(jsonResponse({ message: 'Search error' }, { ok: false, status: 500 }));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText('Search error')).toBeInTheDocument();
  });

  it('shows no definitions message when definitions array is empty', async () => {
    const resultNoDefinitions = { ...searchResult, definitions: [] };
    mockFetchSequence(jsonResponse([resultNoDefinitions]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText('ephemeral')).toBeInTheDocument();
    expect(screen.getByText(/no definitions found/i)).toBeInTheDocument();
  });

  it('encodes the query parameter in the URL', async () => {
    const fetchMock = mockFetchSequence(jsonResponse([]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'over the top');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith(
      '/api/words/search?q=over%20the%20top',
      expect.anything(),
    ));
  });
});
