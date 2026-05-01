import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import { deleteBook, listBooks } from '../../api/client';

export default function ListBooks() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    listBooks()
      .then(setBooks)
      .catch(err => setError(err.message || 'Unable to load books.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleDelete(id) {
    setError('');

    try {
      await deleteBook(id);
      setBooks(prev => prev.filter(b => b.id !== id));
    } catch (err) {
      setError(err.message || 'Unable to delete book.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>The Books are:</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        {loading ? (
          <div className="alert alert-info">Loading books...</div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Id</th>
                <th>Book</th>
                <th>Author</th>
                <th></th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {books.map(book => (
                <tr key={book.id}>
                  <td>{book.id}</td>
                  <td>{book.title}</td>
                  <td>{book.author?.displayName}</td>
                  <td>
                    <Link to={`/books/update/${book.id}`} className="btn btn-primary">Update</Link>
                  </td>
                  <td>
                    <button onClick={() => handleDelete(book.id)} className="btn btn-warning">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!loading && books.length === 0 && (
          <div className="alert alert-secondary">No books found.</div>
        )}
        <Link to="/books/add" className="btn btn-success">Add Book</Link>
      </div>
    </Layout>
  );
}
