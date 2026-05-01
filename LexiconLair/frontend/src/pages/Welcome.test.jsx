import { screen } from '@testing-library/react';
import Welcome from './Welcome';
import { renderWithAuth } from '../test/renderWithAuth';

describe('Welcome', () => {
  it('shows the authenticated user and management links', () => {
    renderWithAuth(<Welcome />, {
      user: { username: 'admin', firstName: 'Ada' },
    });

    expect(screen.getByText('Welcome: Ada')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /manage your users/i })).toHaveAttribute('href', '/users');
    expect(screen.getByRole('link', { name: /manage your words/i })).toHaveAttribute('href', '/words');
    expect(screen.getByRole('link', { name: /manage your authors/i })).toHaveAttribute('href', '/authors');
    expect(screen.getByRole('link', { name: /manage your books/i })).toHaveAttribute('href', '/books');
  });
});
