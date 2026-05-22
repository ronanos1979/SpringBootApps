import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import {
  listWordsWithoutDefinitions,
  refreshMissingWordDefinitions,
  refreshWordDefinitions,
  searchWords,
} from '../../api/client';
import { formatDefinitionLookupReason } from '../../utils/definitionLookup';

export default function WordSearch() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [definitionlessWords, setDefinitionlessWords] = useState([]);
  const [adminLoading, setAdminLoading] = useState(true);
  const [adminError, setAdminError] = useState('');
  const [adminMessage, setAdminMessage] = useState('');
  const [refreshingWordId, setRefreshingWordId] = useState(null);
  const [refreshingAll, setRefreshingAll] = useState(false);

  useEffect(() => {
    loadDefinitionlessWords();
  }, []);

  async function loadDefinitionlessWords() {
    setAdminError('');
    setAdminLoading(true);
    try {
      const data = await listWordsWithoutDefinitions();
      setDefinitionlessWords(data);
    } catch (err) {
      setAdminError(err.message || 'Unable to load words without definitions.');
    } finally {
      setAdminLoading(false);
    }
  }

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

  async function handleRefreshWord(word) {
    setAdminError('');
    setAdminMessage('');
    setRefreshingWordId(word.id);
    try {
      const result = await refreshWordDefinitions(word.id);
      if (result.definitions.length > 0) {
        setDefinitionlessWords(prev => prev.filter(item => item.id !== word.id));
        setAdminMessage(`${word.text} updated with ${result.definitions.length} definition${result.definitions.length === 1 ? '' : 's'}.`);
      } else {
        setAdminMessage(`${word.text} still has no definitions from the external dictionary.`);
      }
    } catch (err) {
      setAdminError(err.message || `Unable to update ${word.text}.`);
    } finally {
      setRefreshingWordId(null);
    }
  }

  async function handleRefreshAll() {
    setAdminError('');
    setAdminMessage('');
    setRefreshingAll(true);
    try {
      const results = await refreshMissingWordDefinitions();
      const updatedIds = new Set(
        results
          .filter(result => result.definitions.length > 0)
          .map(result => result.word.id),
      );
      setDefinitionlessWords(prev => prev.filter(word => !updatedIds.has(word.id)));
      setAdminMessage(`${updatedIds.size} of ${results.length} words updated with definitions.`);
    } catch (err) {
      setAdminError(err.message || 'Unable to update missing definitions.');
    } finally {
      setRefreshingAll(false);
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Word Search</h1>
        <p className="text-muted">Search across all saved words, including words added to books by any user.</p>

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

        <div className="card mb-4">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h2 className="h5 mb-0">Admin</h2>
            <div className="d-flex gap-2">
              <Link to="/admin/settings" className="btn btn-sm btn-outline-primary">Settings</Link>
              <button
                type="button"
                className="btn btn-sm btn-outline-secondary"
                onClick={loadDefinitionlessWords}
                disabled={adminLoading || refreshingAll}
              >
                Refresh List
              </button>
            </div>
          </div>
          <div className="card-body">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div>
                <strong>Words without definitions</strong>
                <span className="badge bg-secondary ms-2">{definitionlessWords.length}</span>
              </div>
              <button
                type="button"
                className="btn btn-warning btn-sm"
                onClick={handleRefreshAll}
                disabled={adminLoading || refreshingAll || definitionlessWords.length === 0}
              >
                {refreshingAll ? 'Updating...' : 'Update All'}
              </button>
            </div>

            {adminError && <div className="alert alert-danger">{adminError}</div>}
            {adminMessage && <div className="alert alert-info">{adminMessage}</div>}

            {adminLoading ? (
              <div className="alert alert-info mb-0">Loading words without definitions...</div>
            ) : definitionlessWords.length === 0 ? (
              <div className="alert alert-success mb-0">All words have definitions.</div>
            ) : (
              <div className="table-responsive">
                <table className="table table-sm align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Word</th>
                      <th>Language</th>
                      <th>Reason</th>
                      <th className="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {definitionlessWords.map(word => (
                      <tr key={word.id}>
                        <td>{word.text}</td>
                        <td>{word.language}</td>
                        <td><small className="text-muted">{formatDefinitionLookupReason(word)}</small></td>
                        <td className="text-end">
                          <button
                            type="button"
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => handleRefreshWord(word)}
                            disabled={refreshingAll || refreshingWordId === word.id}
                          >
                            {refreshingWordId === word.id ? 'Updating...' : 'Update'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        {searched && !loading && results.length === 0 && (
          <div className="alert alert-secondary">No words found matching "{query}".</div>
        )}

        {results.map(result => (
          <div key={result.bookWordId ?? `word-${result.word.id}`} className="card mb-3">
            <div className="card-header d-flex justify-content-between align-items-center">
              <div>
                <strong className="fs-5">{result.word.text}</strong>
                <span className="badge bg-secondary ms-2">{result.word.language}</span>
              </div>
              {result.bookId ? (
                <Link to={`/books/${result.bookId}`} className="btn btn-sm btn-outline-primary">
                  {result.bookTitle}
                  <small className="text-muted ms-1">by {result.authorDisplayName}</small>
                </Link>
              ) : (
                <span className="badge bg-light text-dark border">Saved word</span>
              )}
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
              <div className="card-body text-muted">
                No definitions found.
                <div><small>{formatDefinitionLookupReason(result.word)}</small></div>
              </div>
            )}
          </div>
        ))}
      </div>
    </Layout>
  );
}
