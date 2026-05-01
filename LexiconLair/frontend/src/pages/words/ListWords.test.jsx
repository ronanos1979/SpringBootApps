import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ListWords from './ListWords';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ListWords', () => {
  it('loads words and deletes after backend success', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{ id: 8, text: 'lexicon', language: 'English' }]),
      emptyResponse({ status: 204 }),
    );

    renderWithAuth(<ListWords />, { path: '/words' });

    expect(await screen.findByText('lexicon')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => expect(screen.queryByText('lexicon')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/words', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/words/8', expect.objectContaining({
      method: 'DELETE',
    }));
  });
});
