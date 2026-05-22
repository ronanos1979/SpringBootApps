import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AdminSettings from './AdminSettings';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('AdminSettings', () => {
  it('loads and saves external API throttle settings', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({ externalApiDelayMs: 50, externalApiBatchSize: 10 }),
      jsonResponse({ externalApiDelayMs: 100, externalApiBatchSize: 5 }),
    );

    renderWithAuth(<AdminSettings />, { path: '/admin/settings' });

    const batchInput = await screen.findByLabelText(/external api batch size/i);
    const delayInput = screen.getByLabelText(/external api delay/i);

    await userEvent.clear(batchInput);
    await userEvent.type(batchInput, '5');
    await userEvent.clear(delayInput);
    await userEvent.type(delayInput, '100');
    await userEvent.click(screen.getByRole('button', { name: /save settings/i }));

    expect(await screen.findByText(/settings saved/i)).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/admin/settings',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ externalApiDelayMs: 100, externalApiBatchSize: 5 }),
      }),
    );
  });

  it('shows load errors', async () => {
    mockFetchSequence(jsonResponse({ message: 'Settings unavailable' }, { ok: false, status: 500 }));

    renderWithAuth(<AdminSettings />, { path: '/admin/settings' });

    expect(await screen.findByText('Settings unavailable')).toBeInTheDocument();
  });
});
