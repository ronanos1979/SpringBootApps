import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import {
  createAuthor,
  createBook,
  getBook,
  listAuthors,
  saveBookToCollection,
  searchAuthors,
  searchBooks,
  updateBook,
} from '../../api/client';
import { useAuth } from '../../auth/useAuth';

// Handles both Add (/books/add) and Update (/books/update/:id).
export default function AddBook() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [title, setTitle] = useState('');
  const [authorId, setAuthorId] = useState('');
  const [authorQuery, setAuthorQuery] = useState('');
  const [authors, setAuthors] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitError, setSubmitError] = useState('');
  const [authorError, setAuthorError] = useState('');

  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [savingBook, setSavingBook] = useState(null);
  const suggestionsRef = useRef(null);
  const [authorSuggestions, setAuthorSuggestions] = useState([]);
  const [showAuthorSuggestions, setShowAuthorSuggestions] = useState(false);
  const [showCreateAuthor, setShowCreateAuthor] = useState(false);
  const [newAuthorFirstName, setNewAuthorFirstName] = useState('');
  const [newAuthorLastName, setNewAuthorLastName] = useState('');
  const [creatingAuthor, setCreatingAuthor] = useState(false);
  const authorSuggestionsRef = useRef(null);

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
          setAuthorQuery(book.author?.displayName ?? '');
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

  useEffect(() => {
    if (authorQuery.trim().length < 2 || authorId) {
      setAuthorSuggestions([]);
      setShowAuthorSuggestions(false);
      return;
    }
    const timer = setTimeout(() => {
      searchAuthors(authorQuery.trim())
        .then(results => {
          setAuthorSuggestions(results);
          setShowAuthorSuggestions(results.length > 0);
        })
        .catch(() => {
          const localMatches = authors.filter(author =>
            author.displayName?.toLowerCase().includes(authorQuery.trim().toLowerCase()));
          setAuthorSuggestions(localMatches);
          setShowAuthorSuggestions(localMatches.length > 0);
        });
    }, 300);
    return () => clearTimeout(timer);
  }, [authorQuery, authorId, authors]);

  // Close suggestions on outside click
  useEffect(() => {
    function handleOutsideClick(e) {
      if (suggestionsRef.current && !suggestionsRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
      if (authorSuggestionsRef.current && !authorSuggestionsRef.current.contains(e.target)) {
        setShowAuthorSuggestions(false);
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
      navigate(user?.role === 'ADMIN' ? `/books/${book.id}` : '/');
    } catch (err) {
      setSubmitError(err.message || 'Unable to add book to your collection.');
      setSavingBook(null);
    }
  }

  function splitAuthorName(name) {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return { firstName: '', lastName: '' };
    if (parts.length === 1) return { firstName: parts[0], lastName: '' };
    return {
      firstName: parts.slice(0, -1).join(' '),
      lastName: parts.at(-1),
    };
  }

  function handleSelectAuthor(author) {
    setAuthorId(String(author.id));
    setAuthorQuery(author.displayName);
    setShowAuthorSuggestions(false);
    setShowCreateAuthor(false);
    setAuthorError('');
    setErrors(prev => ({ ...prev, authorId: undefined }));
  }

  function openCreateAuthor() {
    const parsed = splitAuthorName(authorQuery);
    setNewAuthorFirstName(parsed.firstName);
    setNewAuthorLastName(parsed.lastName);
    setShowCreateAuthor(true);
    setShowAuthorSuggestions(false);
    setAuthorError('');
  }

  async function handleCreateAuthor() {
    setAuthorError('');
    if (!newAuthorFirstName.trim() || !newAuthorLastName.trim()) {
      setAuthorError('First name and last name are required.');
      return;
    }

    setCreatingAuthor(true);
    try {
      const created = await createAuthor({
        firstName: newAuthorFirstName.trim(),
        lastName: newAuthorLastName.trim(),
      });
      setAuthors(prev => [...prev, created]);
      handleSelectAuthor(created);
    } catch (err) {
      setAuthorError(err.message || 'Unable to create author.');
    } finally {
      setCreatingAuthor(false);
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
        navigate('/books');
      } else {
        await createBook(book);
        navigate(user?.role === 'ADMIN' ? '/books' : '/');
      }
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
            <label htmlFor="authorQuery" className="form-label">Author:</label>
            <div className="position-relative" ref={authorSuggestionsRef}>
              <input
                id="authorQuery"
                type="text"
                className="form-control"
                value={authorQuery}
                onChange={e => {
                  setAuthorQuery(e.target.value);
                  setAuthorId('');
                  setShowCreateAuthor(false);
                  setErrors(prev => ({ ...prev, authorId: undefined }));
                }}
                onFocus={() => authorSuggestions.length > 0 && setShowAuthorSuggestions(true)}
                placeholder="Search or create an author"
                autoComplete="off"
                required
              />
              {showAuthorSuggestions && (
                <ul
                  className="list-group position-absolute w-100 shadow-sm"
                  style={{ zIndex: 1050, top: '100%', left: 0 }}
                >
                  <li className="list-group-item list-group-item-secondary py-1 small fw-semibold">
                    Existing authors
                  </li>
                  {authorSuggestions.map(author => (
                    <li
                      key={author.id}
                      className="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                      style={{ cursor: 'pointer' }}
                      onMouseDown={() => handleSelectAuthor(author)}
                    >
                      <strong>{author.displayName}</strong>
                      <span className="badge bg-primary rounded-pill">Select</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            {!authorId && authorQuery.trim().length >= 2 && (
              <button type="button" className="btn btn-link p-0 mt-1" onClick={openCreateAuthor}>
                Create author "{authorQuery.trim()}"
              </button>
            )}
            {showCreateAuthor && (
              <div className="border rounded p-3 mt-2">
                <div className="row g-2">
                  <div className="col-md-5">
                    <label htmlFor="newAuthorFirstName" className="form-label">First name</label>
                    <input
                      id="newAuthorFirstName"
                      type="text"
                      className="form-control"
                      value={newAuthorFirstName}
                      onChange={e => setNewAuthorFirstName(e.target.value)}
                    />
                  </div>
                  <div className="col-md-5">
                    <label htmlFor="newAuthorLastName" className="form-label">Last name</label>
                    <input
                      id="newAuthorLastName"
                      type="text"
                      className="form-control"
                      value={newAuthorLastName}
                      onChange={e => setNewAuthorLastName(e.target.value)}
                    />
                  </div>
                  <div className="col-md-2 d-flex align-items-end">
                    <button
                      type="button"
                      className="btn btn-outline-success w-100"
                      onClick={handleCreateAuthor}
                      disabled={creatingAuthor}
                    >
                      {creatingAuthor ? 'Creating…' : 'Create'}
                    </button>
                  </div>
                </div>
                {authorError && <div className="text-warning mt-2">{authorError}</div>}
              </div>
            )}
            {authorId && <div className="form-text text-success">Selected author: {authorQuery}</div>}
            {errors.authorId && <span className="text-warning">{errors.authorId}</span>}
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>Submit</button>
        </form>
      </div>
    </Layout>
  );
}
