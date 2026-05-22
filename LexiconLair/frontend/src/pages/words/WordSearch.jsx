import { useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import { searchWords } from '../../api/client';

export default function WordSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function handleSearch(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    setSearched(true);
    try {
      const data = await searchWords(query);
      setResults(data);
    } catch (err) {
      setError(err.message || 'Search failed.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Word Search</h1>
        <p className="text-muted">Search across all words added to any book by any user.</p>

        <form onSubmit={handleSearch} className="row g-2 mb-4">
          <div className="col-md-8">
            <input
              type="text"
              className="form-control"
              value={query}
              onChange={e => setQuery(e.target.value)}
              placeholder="Search for a word…"
            />
          </div>
          <div className="col-md-2">
            <button type="submit" className="btn btn-primary w-100" disabled={loading}>
              {loading ? 'Searching…' : 'Search'}
            </button>
          </div>
        </form>

        {error && <div className="alert alert-danger">{error}</div>}

        {searched && !loading && results.length === 0 && (
          <div className="alert alert-secondary">No words found matching "{query}".</div>
        )}

        {results.map(result => (
          <div key={result.bookWordId} className="card mb-3">
            <div className="card-header d-flex justify-content-between align-items-center">
              <div>
                <strong className="fs-5">{result.word.text}</strong>
                <span className="badge bg-secondary ms-2">{result.word.language}</span>
              </div>
              <Link to={`/books/${result.bookId}`} className="btn btn-sm btn-outline-primary">
                {result.bookTitle}
                <small className="text-muted ms-1">by {result.authorDisplayName}</small>
              </Link>
            </div>
            {result.definitions && result.definitions.length > 0 ? (
              <ul className="list-group list-group-flush">
                {result.definitions.map(def => (
                  <li key={def.id} className="list-group-item">
                    <span className="badge bg-info text-dark me-2">{def.partOfSpeech}</span>
                    {def.definitionText}
                    {def.example && (
                      <div className="text-muted fst-italic mt-1">
                        <small>"{def.example}"</small>
                      </div>
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <div className="card-body text-muted">No definitions found.</div>
            )}
          </div>
        ))}
      </div>
    </Layout>
  );
}
