import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import { createAuthor, getAuthor, updateAuthor } from '../../api/client';

// Handles both Add (/authors/add) and Update (/authors/update/:id).
export default function AddAuthor() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(Boolean(id));
  const [submitError, setSubmitError] = useState('');

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

  function validate() {
    const errs = {};
    if (!firstName.trim()) errs.firstName = 'First name is required';
    if (!lastName.trim()) errs.lastName = 'Last name is required';
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
        {submitError && <div className="alert alert-danger">{submitError}</div>}
        {loading && <div className="alert alert-info">Loading author...</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="firstName" className="form-label">First Name:</label>
            <input
              id="firstName"
              type="text"
              className="form-control"
              value={firstName}
              onChange={e => setFirstName(e.target.value)}
              required
            />
            {errors.firstName && <span className="text-warning">{errors.firstName}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="lastName" className="form-label">Last Name:</label>
            <input
              id="lastName"
              type="text"
              className="form-control"
              value={lastName}
              onChange={e => setLastName(e.target.value)}
              required
            />
            {errors.lastName && <span className="text-warning">{errors.lastName}</span>}
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>Submit</button>
        </form>
      </div>
    </Layout>
  );
}
