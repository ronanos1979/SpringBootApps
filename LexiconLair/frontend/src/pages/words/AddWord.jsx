import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import { createWord, getWord, updateWord } from '../../api/client';

// Handles both Add (/words/add) and Update (/words/update/:id).
export default function AddWord() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [text, setText] = useState('');
  const [language, setLanguage] = useState('');
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(Boolean(id));
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    if (!id) {
      return;
    }

    getWord(id)
      .then(word => {
        setText(word.text ?? '');
        setLanguage(word.language ?? '');
      })
      .catch(err => setSubmitError(err.message || 'Unable to load word.'))
      .finally(() => setLoading(false));
  }, [id]);

  function validate() {
    const errs = {};
    if (!text.trim()) errs.text = 'Text is required';
    if (!language.trim()) errs.language = 'Language is required';
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
      const word = { text, language };
      if (id) {
        await updateWord(id, word);
      } else {
        await createWord(word);
      }
      navigate('/words');
    } catch (err) {
      setSubmitError(err.message || 'Unable to save word.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>Enter Word Details:</h1>
        {submitError && <div className="alert alert-danger">{submitError}</div>}
        {loading && <div className="alert alert-info">Loading word...</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="text" className="form-label">Text:</label>
            <input
              id="text"
              type="text"
              className="form-control"
              value={text}
              onChange={e => setText(e.target.value)}
              required
            />
            {errors.text && <span className="text-warning">{errors.text}</span>}
          </div>
          <div className="mb-3">
            <label htmlFor="language" className="form-label">Language:</label>
            <input
              id="language"
              type="text"
              className="form-control"
              value={language}
              onChange={e => setLanguage(e.target.value)}
              required
            />
            {errors.language && <span className="text-warning">{errors.language}</span>}
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>Submit</button>
        </form>
      </div>
    </Layout>
  );
}
