import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ListDefinitions from './ListDefinitions';
import { emptyResponse, jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ListDefinitions', () => {
  it('loads definitions and deletes after backend success', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse([{
        id: 5,
        word: { text: 'lexicon', language: 'English' },
        definitionText: 'A vocabulary.',
        partOfSpeech: 'noun',
        example: 'The lexicon is broad.',
        sourceApi: 'https://example.com',
        cachedAt: '2026-05-01T12:00:00',
      }]),
      emptyResponse({ status: 204 }),
    );

    renderWithAuth(<ListDefinitions />, { path: '/definitions' });

    expect(await screen.findByText('A vocabulary.')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /delete/i }));

    await waitFor(() => expect(screen.queryByText('A vocabulary.')).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/definitions', expect.objectContaining({
      credentials: 'include',
    }));
    expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/definitions/5', expect.objectContaining({
      method: 'DELETE',
    }));
  });
});
