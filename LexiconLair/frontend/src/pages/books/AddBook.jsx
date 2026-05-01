import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import { createBook, getBook, listAuthors, updateBook } from '../../api/client';

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

  function validate() {
    const errs = {};
    if (!title.trim()) errs.title = 'Title is required';
    if (!authorId) errs.authorId = 'Author is required';
    return errs;
  }

  async function handleSubmit(e) {
    e.preventDefault();
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
            <input
              id="title"
              type="text"
              className="form-control"
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
            />
            {errors.title && <span className="text-warning">{errors.title}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="authorId" className="form-label">Author:</label>
            <select
              id="authorId"
              className="form-select"
              value={authorId}
              onChange={e => setAuthorId(e.target.value)}
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
