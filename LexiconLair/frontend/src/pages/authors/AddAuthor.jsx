import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import { createAuthor, getAuthor, searchAuthors, updateAuthor } from '../../api/client';

// Handles both Add (/authors/add) and Update (/authors/update/:id).
export default function AddAuthor() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(Boolean(id));
  const [submitError, setSubmitError] = useState('');

  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const suggestionsRef = useRef(null);

  useEffect(() => {
    if (!id) {
      return;
    }

    getAuthor(id)
      .then(author => {
        setFirstName(author.firstName ?? '');
        setLastName(author.lastName ?? '');
      })
      .catch(err => setSubmitError(err.message || 'Unable to load author.'))
      .finally(() => setLoading(false));
  }, [id]);

  // Live search: debounced, fires when combined name has 2+ chars, only in add mode
  useEffect(() => {
    const q = `${firstName} ${lastName}`.trim();
    if (id || q.length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }
    const timer = setTimeout(() => {
      searchAuthors(q)
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
  }, [firstName, lastName, id]);

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

  function handleSelectExistingAuthor(author) {
    setShowSuggestions(false);
    setSubmitError(`"${author.displayName}" already exists. You can select them when adding a book.`);
  }

  function validate() {
    const errs = {};
    if (!firstName.trim()) errs.firstName = 'First name is required';
    if (!lastName.trim()) errs.lastName = 'Last name is required';
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
      const author = { firstName, lastName };
      if (id) {
        await updateAuthor(id, author);
      } else {
        await createAuthor(author);
      }
      navigate('/authors');
    } catch (err) {
      setSubmitError(err.message || 'Unable to save author.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Enter Author:</h1>
        {submitError && <div className="alert alert-warning">{submitError}</div>}
        {loading && <div className="alert alert-info">Loading author...</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="firstName" className="form-label">First Name:</label>
            <input
              id="firstName"
              type="text"
              className="form-control"
              value={firstName}
              onChange={e => { setFirstName(e.target.value); setErrors(prev => ({ ...prev, firstName: undefined })); }}
              autoComplete="off"
              required
            />
            {errors.firstName && <span className="text-warning">{errors.firstName}</span>}
          </div>
          <div className="mb-3" ref={suggestionsRef}>
            <label htmlFor="lastName" className="form-label">Last Name:</label>
            <div className="position-relative">
              <input
                id="lastName"
                type="text"
                className="form-control"
                value={lastName}
                onChange={e => { setLastName(e.target.value); setErrors(prev => ({ ...prev, lastName: undefined })); }}
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
                    Existing authors — this author may already be in the system
                  </li>
                  {suggestions.map(author => (
                    <li
                      key={author.id}
                      className="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                      style={{ cursor: 'pointer' }}
                      onMouseDown={() => handleSelectExistingAuthor(author)}
                    >
                      <span><strong>{author.displayName}</strong></span>
                      <span className="badge bg-warning text-dark rounded-pill">Already exists</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            {errors.lastName && <span className="text-warning">{errors.lastName}</span>}
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>Submit</button>
        </form>
      </div>
    </Layout>
  );
}
