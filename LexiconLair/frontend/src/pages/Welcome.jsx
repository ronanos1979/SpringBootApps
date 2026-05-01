import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { useAuth } from '../auth/useAuth';

// Home route for authenticated users.
export default function Welcome() {
  const { user } = useAuth();
  const name = user?.firstName || user?.username || 'Guest';

  return (
    <Layout>
      <div className="container">
        <div>Welcome to the Lexicon Lair!</div>
        <div>Welcome: {name}</div>
        <hr />
        <div><Link to="/users">Manage your Users</Link></div>
        <div><Link to="/words">Manage your Words</Link></div>
        <div><Link to="/authors">Manage your Authors</Link></div>
        <div><Link to="/books">Manage your Books</Link></div>
      </div>
    </Layout>
  );
}
