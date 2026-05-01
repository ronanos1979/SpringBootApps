export const authors = [
  { id: 1, firstName: 'J.R.R.', lastName: 'Tolkien', displayName: 'J.R.R. Tolkien' },
  { id: 2, firstName: 'George', lastName: 'Orwell', displayName: 'George Orwell' },
  { id: 3, firstName: 'Jane', lastName: 'Austen', displayName: 'Jane Austen' },
];

export const books = [
  { id: 1, title: 'The Fellowship of the Ring', author: { id: 1, displayName: 'J.R.R. Tolkien' } },
  { id: 2, title: 'Nineteen Eighty-Four', author: { id: 2, displayName: 'George Orwell' } },
  { id: 3, title: 'Pride and Prejudice', author: { id: 3, displayName: 'Jane Austen' } },
];

export const words = [
  { id: 1, text: 'Lexicon', language: 'English' },
  { id: 2, text: 'Ephemeral', language: 'English' },
  { id: 3, text: 'Serendipity', language: 'English' },
];

export const users = [
  {
    id: 1, username: 'admin', email: 'admin@example.com',
    firstName: 'Admin', lastName: 'User',
    createdAt: '2024-01-01T10:00:00', createdBy: 'system',
    updatedAt: '2024-01-01T10:00:00', updatedBy: 'system',
  },
  {
    id: 2, username: 'jdoe', email: 'jdoe@example.com',
    firstName: 'John', lastName: 'Doe',
    createdAt: '2024-02-15T09:30:00', createdBy: 'admin',
    updatedAt: '2024-03-01T14:00:00', updatedBy: 'admin',
  },
];

export const definitions = [
  {
    id: 1,
    word: { text: 'Lexicon', language: 'English' },
    definitionText: 'The vocabulary of a person, language, or branch of knowledge.',
    partOfSpeech: 'noun',
    example: 'The lexicon of legal terms.',
    sourceApi: 'https://api.dictionaryapi.dev',
    cachedAt: '2024-03-10T08:00:00',
  },
  {
    id: 2,
    word: { text: 'Ephemeral', language: 'English' },
    definitionText: 'Lasting for a very short time.',
    partOfSpeech: 'adjective',
    example: 'Fashions are ephemeral.',
    sourceApi: 'https://api.dictionaryapi.dev',
    cachedAt: '2024-03-10T08:05:00',
  },
];
