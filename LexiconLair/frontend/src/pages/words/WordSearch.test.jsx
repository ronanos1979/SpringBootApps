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

const savedWordResult = {
  bookWordId: null,
  bookId: null,
  bookTitle: null,
  authorDisplayName: null,
  word: {
    id: 12,
    text: 'stoic',
    language: 'en',
    definitionLookupStatus: 'FAILED',
    definitionLookupHttpStatus: 429,
    definitionLookupMessage: 'error code: 1015',
  },
  definitions: [],
  createdAt: '2026-05-22T10:00:00',
};

const definitionlessWord = {
  id: 14,
  text: 'rakish',
  language: 'en',
  definitionLookupStatus: 'NO_RESULTS',
  definitionLookupHttpStatus: 404,
  definitionLookupMessage: 'No definition found',
};

const refreshedRakishResult = {
  ...savedWordResult,
  word: definitionlessWord,
  definitions: [
    { id: 30, partOfSpeech: 'adjective', definitionText: 'Having a dashing appearance.', example: null },
  ],
};

describe('WordSearch', () => {
  it('renders the search page heading and all-user scope description', () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<WordSearch />);

    expect(screen.getByRole('heading', { name: /word search/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/search for a word/i)).toBeInTheDocument();
    expect(screen.getByText(/all saved words/i)).toBeInTheDocument();
  });

  it('shows results from other users', async () => {
    const otherUserResult = {
      ...searchResult,
      bookWordId: 99,
      word: { id: 10, text: 'ephemeral', language: 'en' },
      createdAt: '2026-05-20T08:00:00',
    };
    mockFetchSequence(jsonResponse([]), jsonResponse([searchResult, otherUserResult]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    // both results rendered — one per bookWordId key
    const cards = await screen.findAllByText('ephemeral');
    expect(cards.length).toBeGreaterThanOrEqual(2);
  });

  it('shows results after search', async () => {
    const fetchMock = mockFetchSequence(jsonResponse([]), jsonResponse([searchResult]));

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
    mockFetchSequence(jsonResponse([]), jsonResponse([searchResult]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    await screen.findByText('ephemeral');

    const bookLink = screen.getByRole('link', { name: /hamlet/i });
    expect(bookLink).toHaveAttribute('href', '/books/1');
    expect(bookLink.textContent).toContain('William Shakespeare');
  });

  it('shows saved words without book context', async () => {
    mockFetchSequence(jsonResponse([]), jsonResponse([savedWordResult]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'stoic');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText('stoic')).toBeInTheDocument();
    expect(screen.getByText('Saved word')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /hamlet/i })).not.toBeInTheDocument();
  });

  it('shows empty state when no results', async () => {
    mockFetchSequence(jsonResponse([]), jsonResponse([]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'zzzunknown');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText(/no words found matching/i)).toBeInTheDocument();
  });

  it('does not show empty state before first search', () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<WordSearch />);

    expect(screen.queryByText(/no words found/i)).not.toBeInTheDocument();
  });

  it('shows error when search fails', async () => {
    mockFetchSequence(jsonResponse([]), jsonResponse({ message: 'Search error' }, { ok: false, status: 500 }));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText('Search error')).toBeInTheDocument();
  });

  it('shows no definitions message when definitions array is empty', async () => {
    const resultNoDefinitions = { ...searchResult, definitions: [] };
    mockFetchSequence(jsonResponse([]), jsonResponse([resultNoDefinitions]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    expect(await screen.findByText('ephemeral')).toBeInTheDocument();
    expect(screen.getByText(/no definitions found/i)).toBeInTheDocument();
    expect(screen.getByText(/no lookup details recorded/i)).toBeInTheDocument();
  });

  it('encodes the query parameter in the URL', async () => {
    const fetchMock = mockFetchSequence(jsonResponse([]), jsonResponse([]));

    renderWithAuth(<WordSearch />);

    await userEvent.type(screen.getByPlaceholderText(/search for a word/i), 'over the top');
    await userEvent.click(screen.getByRole('button', { name: /search/i }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith(
      '/api/words/search?q=over%20the%20top',
      expect.anything(),
    ));
  });

  it('loads words without definitions in the admin panel', async () => {
    mockFetchSequence(jsonResponse([definitionlessWord]));

    renderWithAuth(<WordSearch />);

    expect(await screen.findByText('rakish')).toBeInTheDocument();
    expect(screen.getByText(/no results - HTTP 404 - No definition found/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /update all/i })).toBeInTheDocument();
  });

  it('refreshes one word without definitions', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([definitionlessWord]),
      jsonResponse(refreshedRakishResult),
    );

    renderWithAuth(<WordSearch />);

    expect(await screen.findByText('rakish')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /^update$/i }));

    expect(await screen.findByText(/rakish updated with 1 definition/i)).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('rakish')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/words/14/definitions/refresh',
      expect.objectContaining({ method: 'POST' }),
    );
  });

  it('refreshes all words without definitions', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([definitionlessWord]),
      jsonResponse([refreshedRakishResult]),
    );

    renderWithAuth(<WordSearch />);

    expect(await screen.findByText('rakish')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /update all/i }));

    expect(await screen.findByText(/1 of 1 words updated/i)).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByText('rakish')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/words/definitions/refresh-missing',
      expect.objectContaining({ method: 'POST' }),
    );
  });
});
