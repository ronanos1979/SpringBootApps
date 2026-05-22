import { screen, waitFor, within } from '@testing-library/react';
import Welcome from './Welcome';
import { jsonResponse, mockFetchSequence } from '../test/fetchMock';
import { renderWithAuth } from '../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('Welcome', () => {
  it('shows personalized greeting with firstName', async () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<Welcome />, {
      user: { username: 'admin', firstName: 'Ada' },
    });

    expect(await screen.findByText(/welcome back, ada/i)).toBeInTheDocument();
  });

  it('falls back to username when firstName is absent', async () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<Welcome />, {
      user: { username: 'ronan' },
    });

    expect(await screen.findByText(/welcome back, ronan/i)).toBeInTheDocument();
  });

  it('shows book cards when user has books', async () => {
    mockFetchSequence(jsonResponse([
      { id: 1, title: 'Hamlet', author: { displayName: 'William Shakespeare' } },
      { id: 2, title: 'Frankenstein', author: { displayName: 'Mary Shelley' } },
    ]));

    renderWithAuth(<Welcome />);

    expect(await screen.findByText('Hamlet')).toBeInTheDocument();
    expect(screen.getByText('William Shakespeare')).toBeInTheDocument();
    expect(screen.getByText('Frankenstein')).toBeInTheDocument();
    expect(screen.getByText('Mary Shelley')).toBeInTheDocument();
  });

  it('shows View Words links pointing to book detail routes', async () => {
    mockFetchSequence(jsonResponse([
      { id: 3, title: 'Beloved', author: { displayName: 'Toni Morrison' } },
    ]));

    renderWithAuth(<Welcome />);

    const viewLink = await screen.findByRole('link', { name: /view words/i });
    expect(viewLink).toHaveAttribute('href', '/books/3');
  });

  it('shows empty state when user has no books', async () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<Welcome />);

    expect(await screen.findByText(/you haven't added any books yet/i)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /add your first book/i })).toHaveAttribute('href', '/books/add');
  });

  it('shows error when books fetch fails', async () => {
    mockFetchSequence(jsonResponse({ message: 'Session expired' }, { ok: false, status: 401 }));

    renderWithAuth(<Welcome />);

    expect(await screen.findByText('Session expired')).toBeInTheDocument();
  });

  it('fetches books with mine=true param', async () => {
    const fetchMock = mockFetchSequence(jsonResponse([]));

    renderWithAuth(<Welcome />);

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith(
      '/api/books?mine=true',
      expect.objectContaining({ credentials: 'include' }),
    ));
  });

  it('renders quick action links', async () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<Welcome />);

    await screen.findByText(/welcome back/i);

    const quickActions = screen.getByRole('heading', { name: /quick actions/i }).closest('div');
    expect(quickActions.querySelector('a[href="/words/search"]')).toBeInTheDocument();
    expect(quickActions.querySelector('a[href="/authors"]')).toBeInTheDocument();
  });

  it('renders Add Book and Browse All shortcuts', async () => {
    mockFetchSequence(jsonResponse([]));

    renderWithAuth(<Welcome />);

    await screen.findByText(/welcome back/i);

    expect(screen.getByRole('link', { name: /\+ add book/i })).toHaveAttribute('href', '/books/add');
    expect(screen.getByRole('link', { name: /browse all/i })).toHaveAttribute('href', '/books');
  });

  it('hides admin-only home links for regular users', async () => {
    mockFetchSequence(jsonResponse([
      { id: 3, title: 'Beloved', author: { displayName: 'Toni Morrison' } },
    ]));

    renderWithAuth(<Welcome />, {
      user: { username: 'reader', firstName: 'Rita', role: 'USER' },
    });

    expect(await screen.findByText('Beloved')).toBeInTheDocument();
    const quickActions = screen.getByRole('heading', { name: /quick actions/i }).closest('div');
    expect(within(quickActions).getByRole('link', { name: /add book/i })).toHaveAttribute('href', '/books/add');
    expect(within(quickActions).getByRole('link', { name: /game/i })).toHaveAttribute('href', '/game');
    expect(screen.queryByRole('link', { name: /browse all/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /view words/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /users/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /definitions/i })).not.toBeInTheDocument();
  });
});
