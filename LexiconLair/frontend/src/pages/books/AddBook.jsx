import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import { createBook, getBook, listAuthors, saveBookToCollection, searchBooks, updateBook } from '../../api/client';

// Handles both Add (/books/add) and Update (/books/update/:id).
export default function AddBook() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [authorId, setAuthorId] = useState('');
  const [authors, setAuthors] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState('');

  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [savingBook, setSavingBook] = useState(null);
  const suggestionsRef = useRef(null);

  useEffect(() => {
    const requests = [listAuthors()];
    if (id) {
      requests.push(getBook(id));
    }

    Promise.all(requests)
      .then(([loadedAuthors, book]) => {
        setAuthors(loadedAuthors);
        if (book) {
          setTitle(book.title ?? '');
          setAuthorId(book.author?.id ? String(book.author.id) : '');
        }
      })
      .catch(err => setSubmitError(err.message || 'Unable to load book.'))
      .finally(() => setLoading(false));
  }, [id]);

  // Live search: debounced, only in add mode
  useEffect(() => {
    if (id || title.trim().length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }
    const timer = setTimeout(() => {
      searchBooks(title.trim())
        .then(results => {
          setSuggestions(results);
          setShowSuggestions(results.length > 0);
        })
        .catch(() => {
          setSuggestions([]);
          setShowSuggestions(false);
        });
    }, 300);
    return () => clearTimeout(timer);
  }, [title, id]);

  // Close suggestions on outside click
  useEffect(() => {
    function handleOutsideClick(e) {
      if (suggestionsRef.current && !suggestionsRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  async function handleSelectExistingBook(book) {
    setShowSuggestions(false);
    setSavingBook(book.id);
    try {
      await saveBookToCollection(book.id);
      navigate(`/books/${book.id}`);
    } catch (err) {
      setSubmitError(err.message || 'Unable to add book to your collection.');
      setSavingBook(null);
    }
  }

  function validate() {
    const errs = {};
    if (!title.trim()) errs.title = 'Title is required';
    if (!authorId) errs.authorId = 'Author is required';
    return errs;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setShowSuggestions(false);
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    setSubmitError('');

    try {
      const book = { title, authorId: Number(authorId) };
      if (id) {
        await updateBook(id, book);
      } else {
        await createBook(book);
      }
      navigate('/books');
    } catch (err) {
      setSubmitError(err.message || 'Unable to save book.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Enter Book Details:</h1>
        {submitError && <div className="alert alert-danger">{submitError}</div>}
        {loading && <div className="alert alert-info">Loading book...</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="title" className="form-label">Title:</label>
            <div className="position-relative" ref={suggestionsRef}>
              <input
                id="title"
                type="text"
                className="form-control"
                value={title}
                onChange={e => { setTitle(e.target.value); setErrors(prev => ({ ...prev, title: undefined })); }}
                onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
                autoComplete="off"
                required
              />
              {showSuggestions && (
                <ul
                  className="list-group position-absolute w-100 shadow-sm"
                  style={{ zIndex: 1050, top: '100%', left: 0 }}
                >
                  <li className="list-group-item list-group-item-secondary py-1 small fw-semibold">
                    Existing books — click to add to your list
                  </li>
                  {suggestions.map(book => (
                    <li
                      key={book.id}
                      className="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                      style={{ cursor: 'pointer' }}
                      onMouseDown={() => handleSelectExistingBook(book)}
                    >
                      <span>
                        <strong>{book.title}</strong>
                        {book.author?.displayName && (
                          <span className="text-muted ms-2 small">by {book.author.displayName}</span>
                        )}
                      </span>
                      <span className="badge bg-primary rounded-pill">
                        {savingBook === book.id ? 'Adding…' : '+ Add to my list'}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            {errors.title && <span className="text-warning">{errors.title}</span>}
            {!id && title.trim().length >= 2 && !showSuggestions && suggestions.length === 0 && (
              <div className="form-text text-muted">No existing books found with this title.</div>
            )}
          </div>
          <div className="mb-3">
            <label htmlFor="authorId" className="form-label">Author:</label>
            <select
              id="authorId"
              className="form-select"
              value={authorId}
              onChange={e => { setAuthorId(e.target.value); setErrors(prev => ({ ...prev, authorId: undefined })); }}
            >
              <option value="">-- Select an author --</option>
              {authors.map(author => (
                <option key={author.id} value={author.id}>
                  {author.displayName}
                </option>
              ))}
            </select>
            {errors.authorId && <span className="text-warning">{errors.authorId}</span>}
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>Submit</button>
        </form>
      </div>
    </Layout>
  );
}
