import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import { deleteUser, listUsers } from '../../api/client';

export default function ListUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    listUsers()
      .then(setUsers)
      .catch(err => setError(err.message || 'Unable to load users.'))
      .finally(() => setLoading(false));
  }, []);

  async function handleDelete(id) {
    setError('');

    try {
      await deleteUser(id);
      setUsers(prev => prev.filter(u => u.id !== id));
    } catch (err) {
      setError(err.message || 'Unable to delete user.');
    }
  }

  return (
    <Layout>
      <div className="container">
        <h1>The App Users are:</h1>
        {error && <div className="alert alert-danger">{error}</div>}
        {loading ? (
          <div className="alert alert-info">Loading users...</div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Id</th>
                  <th>Username</th>
                  <th>Email</th>
                  <th>First Name</th>
                  <th>Last Name</th>
                  <th>Role</th>
                  <th>Created At</th>
                  <th>Created By</th>
                  <th>Updated At</th>
                  <th>Updated By</th>
                  <th></th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>{user.email}</td>
                    <td>{user.firstName}</td>
                    <td>{user.lastName}</td>
                    <td>
                      <span className={`badge ${user.role === 'ADMIN' ? 'bg-danger' : 'bg-secondary'}`}>
                        {user.role === 'ADMIN' ? 'Admin' : 'Regular User'}
                      </span>
                    </td>
                    <td>{user.createdAt}</td>
                    <td>{user.createdBy}</td>
                    <td>{user.updatedAt}</td>
                    <td>{user.updatedBy}</td>
                    <td>
                      <Link to={`/users/update/${user.id}`} className="btn btn-primary">Update</Link>
                    </td>
                    <td>
                      <button onClick={() => handleDelete(user.id)} className="btn btn-warning">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && users.length === 0 && (
          <div className="alert alert-secondary">No users found.</div>
        )}
        <Link to="/users/add" className="btn btn-success">Add User</Link>
      </div>
    </Layout>
  );
}
