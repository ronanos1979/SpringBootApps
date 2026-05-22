import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import { deleteAuthor, listAuthors } from '../../api/client';

export default function ListAuthors() {
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    listAuthors()
      .then(setAuthors)
      .catch(err => setError(err.message || 'Unable to load authors.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleDelete(id) {
    setError('');

    try {
      await deleteAuthor(id);
      setAuthors(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      setError(err.message || 'Unable to delete author.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>The Authors are:</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        {loading ? (
          <div className="alert alert-info">Loading authors...</div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Id</th>
                  <th>First Name</th>
                  <th>Last Name</th>
                  <th>Created At</th>
                  <th>Created By</th>
                  <th>Updated At</th>
                  <th>Updated By</th>
                  <th></th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {authors.map(author => (
                  <tr key={author.id}>
                    <td>{author.id}</td>
                    <td>{author.firstName}</td>
                    <td>{author.lastName}</td>
                    <td>{author.createdAt}</td>
                    <td>{author.createdBy}</td>
                    <td>{author.updatedAt}</td>
                    <td>{author.updatedBy}</td>
                    <td>
                      <Link to={`/authors/update/${author.id}`} className="btn btn-primary">
                        Update
                      </Link>
                    </td>
                    <td>
                      <button onClick={() => handleDelete(author.id)} className="btn btn-warning">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && authors.length === 0 && (
          <div className="alert alert-secondary">No authors found.</div>
        )}
        <Link to="/authors/add" className="btn btn-success">Add Author</Link>
      </div>
    </Layout>
  );
}
