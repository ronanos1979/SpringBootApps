import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { useAuth } from '../auth/useAuth';
import { listBooks } from '../api/client';

export default function Welcome() {
  const { user } = useAuth();
  const name = user?.firstName || user?.username || 'Guest';
  const isAdmin = user?.role === 'ADMIN';

  const [myBooks, setMyBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    listBooks(true)
      .then(setMyBooks)
      .catch(err => setError(err.message || 'Unable to load your books.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="container">

        <div className="py-4 mb-4 border-bottom">
          <h1 className="display-6">Welcome back, {name}!</h1>
          <p className="text-muted mb-0">Pick up where you left off, or start something new.</p>
        </div>

        <div className="d-flex justify-content-between align-items-center mb-3">
          <h2 className="h4 mb-0">Your Books</h2>
          <div className="d-flex gap-2">
            <Link to="/books/add" className="btn btn-success btn-sm">+ Add Book</Link>
            {isAdmin && <Link to="/books" className="btn btn-outline-secondary btn-sm">Browse All</Link>}
          </div>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        {loading ? (
          <div className="alert alert-info">Loading your books…</div>
        ) : myBooks.length === 0 ? (
          <div className="card text-center py-5">
            <div className="card-body">
              <p className="text-muted mb-3">You haven't added any books yet.</p>
              <Link to="/books/add" className="btn btn-success">Add Your First Book</Link>
            </div>
          </div>
        ) : (
          <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-3 mb-5">
            {myBooks.map(book => (
              <div key={book.id} className="col">
                <div className="card h-100 shadow-sm">
                  <div className="card-body">
                    <h5 className="card-title mb-1">{book.title}</h5>
                    <p className="card-text text-muted small">{book.author?.displayName}</p>
                  </div>
                  {isAdmin && (
                    <div className="card-footer bg-transparent border-top-0">
                      <Link to={`/books/${book.id}`} className="btn btn-primary btn-sm w-100">
                        View Words
                      </Link>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        <hr />

        <div className="mt-4">
          <h2 className="h5 mb-3">Quick Actions</h2>
          <div className="d-flex gap-2 flex-wrap">
            <Link to="/game" className="btn btn-outline-primary">Game</Link>
            <Link to="/books/add" className="btn btn-outline-success">Add Book</Link>
            {isAdmin && (
              <>
                <Link to="/words/search" className="btn btn-outline-primary">Word Search</Link>
                <Link to="/authors" className="btn btn-outline-secondary">Authors</Link>
                <Link to="/definitions" className="btn btn-outline-secondary">Definitions</Link>
                <Link to="/users" className="btn btn-outline-secondary">Users</Link>
              </>
            )}
          </div>
        </div>

      </div>
    </Layout>
  );
}
