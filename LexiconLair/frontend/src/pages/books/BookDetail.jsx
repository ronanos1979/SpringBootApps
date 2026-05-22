import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import { getBook, getBookWords, addWordToBook, bulkAddWordsToBook, removeWordFromBook } from '../../api/client';
import { formatDefinitionLookupReason } from '../../utils/definitionLookup';

export default function BookDetail() {
  const { id } = useParams();
  const [book, setBook] = useState(null);
  const [bookWords, setBookWords] = useState([]);
  const [showMine, setShowMine] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Single add form
  const [addMode, setAddMode] = useState('single'); // 'single' | 'bulk'
  const [wordText, setWordText] = useState('');
  const [wordLanguage, setWordLanguage] = useState('en');
  const [adding, setAdding] = useState(false);
  const [addError, setAddError] = useState('');

  // Bulk add form
  const [bulkText, setBulkText] = useState('');
  const bulkTextRef = useRef('');
  const [bulkLanguage, setBulkLanguage] = useState('en');
  const [bulkAdding, setBulkAdding] = useState(false);
  const [bulkResult, setBulkResult] = useState(null);
  const [bulkError, setBulkError] = useState('');

  useEffect(() => {
    Promise.all([getBook(id), getBookWords(id, true)])
      .then(([bookData, wordsData]) => {
        setBook(bookData);
        setBookWords(wordsData);
      })
      .catch(err => setError(err.message || 'Unable to load book.'))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (loading) return;
    getBookWords(id, showMine)
      .then(setBookWords)
      .catch(err => setError(err.message || 'Unable to load words.'));
  }, [showMine]);

  async function handleAddWord(e) {
    e.preventDefault();
    setAddError('');
    if (!wordText.trim()) return;
    setAdding(true);
    try {
      const created = await addWordToBook(id, { text: wordText.trim(), language: wordLanguage });
      setWordText('');
      if (showMine) {
        setBookWords(prev => [created, ...prev]);
      }
    } catch (err) {
      setAddError(err.message || 'Unable to add word.');
    } finally {
      setAdding(false);
    }
  }

  async function handleBulkAdd(e) {
    e.preventDefault();
    setBulkResult(null);
    setBulkError('');

    const words = bulkTextRef.current
      .split(/[\n,]+/)
      .map(w => w.trim())
      .filter(Boolean);

    if (words.length === 0) return;

    setBulkAdding(true);
    try {
      const result = await bulkAddWordsToBook(id, { words, language: bulkLanguage });
      setBulkResult(result);
      setBulkText('');
      bulkTextRef.current = '';
      if (showMine && result.added.length > 0) {
        setBookWords(prev => [...result.added, ...prev]);
      }
    } catch (err) {
      setBulkError(err.message || 'Unable to add words.');
    } finally {
      setBulkAdding(false);
    }
  }

  async function handleRemove(bookWordId) {
    setError('');
    try {
      await removeWordFromBook(id, bookWordId);
      setBookWords(prev => prev.filter(bw => bw.id !== bookWordId));
    } catch (err) {
      setError(err.message || 'Unable to remove word.');
    }
  }

  function switchMode(mode) {
    setAddMode(mode);
    setAddError('');
    setBulkResult(null);
    setBulkError('');
  }

  if (loading) {
    return <Layout><div className="container"><div className="alert alert-info">Loading...</div></div></Layout>;
  }

  return (
    <Layout>
      <div className="container">
        {error && <div className="alert alert-danger">{error}</div>}

        {book && (
          <div className="mb-3">
            <h1>{book.title}</h1>
            <p className="text-muted">by {book.author?.displayName}</p>
            <Link to="/books" className="btn btn-outline-secondary btn-sm">← Back to Books</Link>
          </div>
        )}

        <div className="card mb-4">
          <div className="card-header p-0 border-bottom-0">
            <ul className="nav nav-tabs px-3 pt-2">
              <li className="nav-item">
                <button
                  type="button"
                  className={`nav-link${addMode === 'single' ? ' active' : ''}`}
                  onClick={() => switchMode('single')}
                >
                  Add a Word
                </button>
              </li>
              <li className="nav-item">
                <button
                  type="button"
                  className={`nav-link${addMode === 'bulk' ? ' active' : ''}`}
                  onClick={() => switchMode('bulk')}
                >
                  Add Multiple Words
                </button>
              </li>
            </ul>
          </div>

          <div className="card-body">
            {addMode === 'single' ? (
              <>
                {addError && <div className="alert alert-danger">{addError}</div>}
                <form onSubmit={handleAddWord} className="row g-2 align-items-end">
                  <div className="col-md-5">
                    <label className="form-label">Word</label>
                    <input
                      type="text"
                      className="form-control"
                      value={wordText}
                      onChange={e => setWordText(e.target.value)}
                      placeholder="e.g. ephemeral"
                      required
                    />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label">Language</label>
                    <input
                      type="text"
                      className="form-control"
                      value={wordLanguage}
                      onChange={e => setWordLanguage(e.target.value)}
                      placeholder="en"
                      maxLength={20}
                      required
                    />
                  </div>
                  <div className="col-md-2">
                    <button type="submit" className="btn btn-success w-100" disabled={adding}>
                      {adding ? 'Adding…' : 'Add Word'}
                    </button>
                  </div>
                </form>
              </>
            ) : (
              <>
                {bulkError && <div className="alert alert-danger">{bulkError}</div>}
                <form onSubmit={handleBulkAdd}>
                  <div className="mb-3">
                    <label htmlFor="bulkWords" className="form-label">Words</label>
                    <textarea
                      id="bulkWords"
                      className="form-control font-monospace"
                      rows={8}
                      value={bulkText}
                      onChange={e => {
                        bulkTextRef.current = e.target.value;
                        setBulkText(e.target.value);
                        setBulkResult(null);
                      }}
                      placeholder={'One word per line, or comma separated:\n\nephemeral\nserendipity\nubiquitous\n\nor: ephemeral, serendipity, ubiquitous'}
                    />
                    <div className="form-text">Separate words by new lines or commas. Duplicates are ignored.</div>
                  </div>
                  <div className="row g-2 align-items-end">
                    <div className="col-md-3">
                      <label className="form-label">Language</label>
                      <input
                        type="text"
                        className="form-control"
                        value={bulkLanguage}
                        onChange={e => setBulkLanguage(e.target.value)}
                        placeholder="en"
                        maxLength={20}
                        required
                      />
                    </div>
                    <div className="col-auto">
                      <button type="submit" className="btn btn-success" disabled={bulkAdding}>
                        {bulkAdding ? 'Adding…' : 'Add Words'}
                      </button>
                    </div>
                  </div>
                </form>

                {bulkResult && (
                  <div className="mt-3">
                    {bulkResult.added.length > 0 && (
                      <div className="alert alert-success mb-2">
                        <strong>{bulkResult.added.length}</strong>{' '}
                        {bulkResult.added.length === 1 ? 'word' : 'words'} added successfully.
                      </div>
                    )}
                    {bulkResult.duplicates.length > 0 && (
                      <div className="alert alert-warning mb-2">
                        <strong>{bulkResult.duplicates.length}</strong> already in this book:{' '}
                        <span className="fst-italic">{bulkResult.duplicates.join(', ')}</span>
                      </div>
                    )}
                    {bulkResult.errors.length > 0 && (
                      <div className="alert alert-danger mb-2">
                        <strong>{bulkResult.errors.length}</strong> failed to add:{' '}
                        <span className="fst-italic">{bulkResult.errors.join(', ')}</span>
                      </div>
                    )}
                    {bulkResult.added.length === 0 &&
                      bulkResult.duplicates.length === 0 &&
                      bulkResult.errors.length === 0 && (
                        <div className="alert alert-secondary">No words were processed. Check your input.</div>
                      )}
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        <div className="d-flex justify-content-between align-items-center mb-3">
          <h4>Words ({bookWords.length})</h4>
          <div className="btn-group" role="group">
            <button
              className={`btn btn-sm ${showMine ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setShowMine(true)}
            >
              My Words
            </button>
            <button
              className={`btn btn-sm ${!showMine ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setShowMine(false)}
            >
              All Words
            </button>
          </div>
        </div>

        {bookWords.length === 0 ? (
          <div className="alert alert-secondary">
            {showMine ? 'You have not added any words to this book yet.' : 'No words have been added to this book yet.'}
          </div>
        ) : (
          bookWords.map(bw => (
            <div key={bw.id} className="card mb-3">
              <div className="card-header d-flex justify-content-between align-items-center">
                <div>
                  <strong className="fs-5">{bw.word.text}</strong>
                  <span className="badge bg-secondary ms-2">{bw.word.language}</span>
                  {!showMine && (
                    <small className="text-muted ms-2">added by user {bw.createdBy}</small>
                  )}
                </div>
                {showMine && (
                  <button
                    className="btn btn-sm btn-outline-danger"
                    onClick={() => handleRemove(bw.id)}
                  >
                    Remove
                  </button>
                )}
              </div>
              {bw.definitions && bw.definitions.length > 0 ? (
                <ul className="list-group list-group-flush">
                  {bw.definitions.map(def => (
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
                  <div><small>{formatDefinitionLookupReason(bw.word)}</small></div>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </Layout>
  );
}
