import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { deleteDefinition, listDefinitions } from '../../api/client';

export default function ListDefinitions() {
  const [definitions, setDefinitions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    listDefinitions()
      .then(setDefinitions)
      .catch(err => setError(err.message || 'Unable to load definitions.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleDelete(id) {
    setError('');

    try {
      await deleteDefinition(id);
      setDefinitions(prev => prev.filter(d => d.id !== id));
    } catch (err) {
      setError(err.message || 'Unable to delete definition.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>The Definitions are:</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        {loading ? (
          <div className="alert alert-info">Loading definitions...</div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Id</th>
                  <th>Word</th>
                  <th>Language</th>
                  <th>Definition Text</th>
                  <th>Part Of Speech</th>
                  <th>Example</th>
                  <th>Source API</th>
                  <th>Cached At</th>
                  <th>Created At</th>
                  <th>Created By</th>
                  <th>Updated At</th>
                  <th>Updated By</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {definitions.map(def => (
                  <tr key={def.id}>
                    <td>{def.id}</td>
                    <td>{def.word?.text}</td>
                    <td>{def.word?.language}</td>
                    <td>{def.definitionText}</td>
                    <td>{def.partOfSpeech}</td>
                    <td>{def.example}</td>
                    <td>
                      <a href={def.sourceApi} target="_blank" rel="noreferrer">{def.sourceApi}</a>
                    </td>
                    <td>{def.cachedAt}</td>
                    <td>{def.createdAt}</td>
                    <td>{def.createdBy}</td>
                    <td>{def.updatedAt}</td>
                    <td>{def.updatedBy}</td>
                    <td>
                      <button onClick={() => handleDelete(def.id)} className="btn btn-warning">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && definitions.length === 0 && (
          <div className="alert alert-secondary">No definitions found.</div>
        )}
      </div>
    </Layout>
  );
}
