import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AddWord from './AddWord';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('AddWord', () => {
  it('posts a new word and navigates back to words', async () => {
    const fetchMock = mockFetchSequence(jsonResponse({ id: 8, text: 'lexicon', language: 'English' }, {
      status: 201,
    }));

    renderWithAuth(<AddWord />, {
      initialEntries: ['/words/add'],
      path: '/words/add',
      routeAfterSubmit: '/words',
    });

    await userEvent.type(screen.getByLabelText(/text/i), 'lexicon');
    await userEvent.type(screen.getByLabelText(/language/i), 'English');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('/words route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledWith('/api/words', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ text: 'lexicon', language: 'English' }),
    }));
  });

  it('loads an existing word and sends an update', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({ id: 8, text: 'oldword', language: 'English' }),
      jsonResponse({ id: 8, text: 'newword', language: 'English' }),
    );

    renderWithAuth(<AddWord />, {
      initialEntries: ['/words/update/8'],
      path: '/words/update/:id',
      routeAfterSubmit: '/words',
    });

    const text = await screen.findByLabelText(/text/i);
    await userEvent.clear(text);
    await userEvent.type(text, 'newword');
    await userEvent.click(screen.getByRole('button', { name: /submit/i }));

    await waitFor(() => expect(screen.getByText('/words route')).toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/words/8', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/words/8', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ text: 'newword', language: 'English' }),
    }));
  });
});
