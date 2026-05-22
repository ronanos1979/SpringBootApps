import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

export default function Navbar() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  async function handleLogout() {
    await logout();
    navigate('/login?logout', { replace: true });
  }

  return (
    <nav className="navbar navbar-expand-md navbar-light bg-light mb-3 p-1">
      <a className="navbar-brand m-1" href="https://www.ronanos.com" target="_blank" rel="noreferrer">
        ronanos.com
      </a>
      <div className="collapse navbar-collapse">
        <ul className="navbar-nav">
          <li className="nav-item"><Link className="nav-link" to="/">Home</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/books">Books</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/words/search">Word Search</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/game">Game</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/authors">Authors</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/words">All Words</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/definitions">Definitions</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/users">Users</Link></li>
          <li className="nav-item"><Link className="nav-link" to="/admin/settings">Admin</Link></li>
        </ul>
      </div>
      <ul className="navbar-nav">
        <li className="nav-item">
          <button onClick={handleLogout} className="btn btn-link nav-link">Logout</button>
        </li>
      </ul>
    </nav>
  );
}
