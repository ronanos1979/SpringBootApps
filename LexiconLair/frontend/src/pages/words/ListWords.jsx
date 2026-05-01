import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import { deleteWord, listWords } from '../../api/client';

export default function ListWords() {
  const [words, setWords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    listWords()
      .then(setWords)
      .catch(err => setError(err.message || 'Unable to load words.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleDelete(id) {
    setError('');

    try {
      await deleteWord(id);
      setWords(prev => prev.filter(w => w.id !== id));
    } catch (err) {
      setError(err.message || 'Unable to delete word.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>The Words are:</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        {loading ? (
          <div className="alert alert-info">Loading words...</div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Id</th>
                <th>Text</th>
                <th>Language</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {words.map(word => (
                <tr key={word.id}>
                  <td>{word.id}</td>
                  <td>{word.text}</td>
                  <td>{word.language}</td>
                  <td>
                    <Link to={`/words/update/${word.id}`} className="btn btn-primary">Update</Link>
                  </td>
                  <td>
                    <button onClick={() => handleDelete(word.id)} className="btn btn-warning">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!loading && words.length === 0 && (
          <div className="alert alert-secondary">No words found.</div>
        )}
        <Link to="/words/add" className="btn btn-success">Add Word</Link>
      </div>
    </Layout>
  );
}
