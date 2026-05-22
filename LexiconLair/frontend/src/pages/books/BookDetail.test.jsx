import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import BookDetail from './BookDetail';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

const bulkResult = (added = [], duplicates = [], errors = []) =>
  jsonResponse({ added, duplicates, errors });

afterEach(() => {
  vi.restoreAllMocks();
});

const book = { id: 1, title: 'Hamlet', author: { displayName: 'William Shakespeare' } };

const bookWord = {
  id: 5,
  word: { id: 2, text: 'ephemeral', language: 'en' },
  definitions: [
    { id: 3, partOfSpeech: 'adjective', definitionText: 'Lasting a very short time.', example: 'An ephemeral joy.' },
  ],
  createdAt: '2026-05-22T10:00:00',
  createdBy: 1,
};

describe('BookDetail', () => {
  it('renders book title and author on load', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([bookWord]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    expect(await screen.findByText('Hamlet')).toBeInTheDocument();
    expect(screen.getByText('by William Shakespeare')).toBeInTheDocument();
  });

  it('renders word cards with definitions', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([bookWord]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    expect(await screen.findByText('ephemeral')).toBeInTheDocument();
    expect(screen.getByText('Lasting a very short time.')).toBeInTheDocument();
    expect(screen.getByText(/"An ephemeral joy\."/i)).toBeInTheDocument();
  });

  it('shows empty state when no words added', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    expect(await screen.findByText(/you have not added any words/i)).toBeInTheDocument();
  });

  it('adds a word and prepends it to the list', async () => {
    const newBookWord = {
      id: 10,
      word: { id: 9, text: 'serendipity', language: 'en' },
      definitions: [],
      createdAt: '2026-05-22T11:00:00',
      createdBy: 1,
    };

    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      jsonResponse(newBookWord, { status: 201 }),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');

    await userEvent.type(screen.getByPlaceholderText(/e\.g\. ephemeral/i), 'serendipity');
    await userEvent.click(screen.getByRole('button', { name: /add word/i }));

    expect(await screen.findByText('serendipity')).toBeInTheDocument();
  });

  it('shows conflict error when word already added', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      jsonResponse({ message: 'Word already added to this book' }, { ok: false, status: 409 }),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');

    await userEvent.type(screen.getByPlaceholderText(/e\.g\. ephemeral/i), 'ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /add word/i }));

    expect(await screen.findByText('Word already added to this book')).toBeInTheDocument();
  });

  it('removes a word from the list after delete', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([bookWord]),
      emptyResponse({ status: 204 }),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    expect(await screen.findByText('ephemeral')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /remove/i }));

    await waitFor(() => expect(screen.queryByText('ephemeral')).not.toBeInTheDocument());
  });

  it('toggles to All Words and reloads words', async () => {
    const allWord = {
      id: 6,
      word: { id: 3, text: 'ubiquitous', language: 'en' },
      definitions: [],
      createdAt: '2026-05-22T09:00:00',
      createdBy: 2,
    };

    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([bookWord]),
      jsonResponse([bookWord, allWord]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('ephemeral');

    await userEvent.click(screen.getByRole('button', { name: /all words/i }));

    expect(await screen.findByText('ubiquitous')).toBeInTheDocument();
    expect(screen.getByText(/added by user 2/i)).toBeInTheDocument();
  });

  it('shows no definitions message when definitions array is empty', async () => {
    const wordNoDefinitions = { ...bookWord, definitions: [] };

    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([wordNoDefinitions]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    expect(await screen.findByText('ephemeral')).toBeInTheDocument();
    expect(screen.getByText(/no definitions found/i)).toBeInTheDocument();
  });

  it('shows load error when initial fetch fails', async () => {
    mockFetchSequence(
      jsonResponse({ message: 'Book not found' }, { ok: false, status: 404 }),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/99'] });

    expect(await screen.findByText('Book not found')).toBeInTheDocument();
  });

  // Bulk add tests

  it('shows bulk add tab and switches to it', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');

    expect(screen.getByRole('button', { name: /add a word/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /add multiple words/i })).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /add multiple words/i }));

    expect(screen.getByRole('textbox', { name: /words/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/one word per line/i)).toBeInTheDocument();
  });

  it('bulk adds comma-separated words and shows success summary', async () => {
    const addedWord = {
      id: 20,
      word: { id: 9, text: 'serendipity', language: 'en' },
      definitions: [],
      createdAt: '2026-05-22T11:00:00',
      createdBy: 1,
    };

    const fetchMock = mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      bulkResult([addedWord], ['ephemeral'], []),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');
    await userEvent.click(screen.getByRole('button', { name: /add multiple words/i }));

    await userEvent.type(screen.getByRole('textbox', { name: /words/i }), 'serendipity, ephemeral');
    await userEvent.click(screen.getByRole('button', { name: /add words/i }));

    await waitFor(() => {
      expect(document.querySelector('.alert-success')).toHaveTextContent(/1\s+word added successfully/i);
      expect(document.querySelector('.alert-warning')).toHaveTextContent(/1\s+already in this book/i);
      expect(document.querySelector('.alert-warning')).toHaveTextContent(/ephemeral/i);
    });

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/books/1/words/bulk',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ words: ['serendipity', 'ephemeral'], language: 'en' }),
      }),
    );
  });

  it('bulk adds newline-separated words', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      bulkResult([], [], []),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');
    await userEvent.click(screen.getByRole('button', { name: /add multiple words/i }));

    await userEvent.type(
      screen.getByRole('textbox', { name: /words/i }),
      'ephemeral{enter}serendipity{enter}ubiquitous',
    );
    await userEvent.click(screen.getByRole('button', { name: /add words/i }));

    await waitFor(() =>
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/books/1/words/bulk',
        expect.objectContaining({
          body: JSON.stringify({ words: ['ephemeral', 'serendipity', 'ubiquitous'], language: 'en' }),
        }),
      ),
    );
  });

  it('prepends bulk-added words to the list when in My Words mode', async () => {
    const newWord = {
      id: 20,
      word: { id: 9, text: 'serendipity', language: 'en' },
      definitions: [],
      createdAt: '2026-05-22T11:00:00',
      createdBy: 1,
    };

    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      bulkResult([newWord], [], []),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');
    await userEvent.click(screen.getByRole('button', { name: /add multiple words/i }));

    await userEvent.type(screen.getByRole('textbox', { name: /words/i }), 'serendipity');
    await userEvent.click(screen.getByRole('button', { name: /add words/i }));

    expect(await screen.findByText('serendipity')).toBeInTheDocument();
  });

  it('shows error summary when some words fail', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      bulkResult([], [], ['unknownword']),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');
    await userEvent.click(screen.getByRole('button', { name: /add multiple words/i }));

    await userEvent.type(screen.getByRole('textbox', { name: /words/i }), 'unknownword');
    await userEvent.click(screen.getByRole('button', { name: /add words/i }));

    await waitFor(() => {
      expect(document.querySelector('.alert-danger')).toHaveTextContent(/1\s+failed to add/i);
      expect(document.querySelector('.alert-danger')).toHaveTextContent(/unknownword/i);
    });
  });

  it('shows empty-input result message when nothing is processed', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
      bulkResult([], [], []),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');
    await userEvent.click(screen.getByRole('button', { name: /add multiple words/i }));
    await userEvent.type(screen.getByRole('textbox', { name: /words/i }), 'x');
    await userEvent.click(screen.getByRole('button', { name: /add words/i }));

    expect(await screen.findByText(/no words were processed/i)).toBeInTheDocument();
  });

  it('has back to books link', async () => {
    mockFetchSequence(
      jsonResponse(book),
      jsonResponse([]),
    );

    renderWithAuth(<BookDetail />, { path: '/books/:id', initialEntries: ['/books/1'] });

    await screen.findByText('Hamlet');

    expect(screen.getByRole('link', { name: /← back to books/i })).toHaveAttribute('href', '/books');
  });
});
