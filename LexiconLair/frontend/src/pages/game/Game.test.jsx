import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Game from './Game';
import { jsonResponse, mockFetchSequence } from '../../test/fetchMock';
import { renderWithAuth } from '../../test/renderWithAuth';

afterEach(() => {
  vi.restoreAllMocks();
});

const question = {
  mode: 'easy',
  wordId: 1,
  wordText: 'stoic',
  language: 'en',
  correctDefinitionId: 10,
  options: [
    { definitionId: 10, definitionText: 'Enduring pain without complaint.', partOfSpeech: 'adjective' },
    { definitionId: 20, definitionText: 'Having a dashing appearance.', partOfSpeech: 'adjective' },
  ],
};

describe('Game', () => {
  it('starts an easy game and marks the correct answer', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({ gameQuestionCount: 2 }),
      jsonResponse(question),
    );

    renderWithAuth(<Game />, { path: '/game' });

    await userEvent.click(screen.getByRole('button', { name: /start game/i }));

    expect(await screen.findByText('stoic')).toBeInTheDocument();
    expect(screen.getByText('1/2')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /enduring pain/i }));

    expect(screen.getByText('Correct.')).toBeInTheDocument();
    expect(screen.getByLabelText('Correct')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/game/question?mode=easy',
      expect.objectContaining({ credentials: 'include' }),
    );
  });

  it('loads difficult mode questions', async () => {
    const fetchMock = mockFetchSequence(
      jsonResponse({ gameQuestionCount: 10 }),
      jsonResponse({ ...question, mode: 'difficult' }),
    );

    renderWithAuth(<Game />, { path: '/game' });

    await userEvent.click(screen.getByRole('button', { name: /difficult/i }));

    expect(await screen.findByText('stoic')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/game/question?mode=difficult',
      expect.objectContaining({ credentials: 'include' }),
    );
  });

  it('shows results after the final question', async () => {
    mockFetchSequence(
      jsonResponse({ gameQuestionCount: 1 }),
      jsonResponse(question),
    );

    renderWithAuth(<Game />, { path: '/game' });

    await userEvent.click(screen.getByRole('button', { name: /start game/i }));
    await screen.findByText('stoic');
    await userEvent.click(screen.getByRole('button', { name: /having a dashing/i }));

    expect(screen.getByText('Not quite.')).toBeInTheDocument();
    expect(screen.getByLabelText('Incorrect')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /show results/i }));

    expect(screen.getByRole('heading', { name: /results/i })).toBeInTheDocument();
    expect(screen.getByText('0/1')).toBeInTheDocument();
  });
});
