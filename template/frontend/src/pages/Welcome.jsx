import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { useAuth } from '../auth/useAuth';

export default function Welcome() {
  const { user } = useAuth();
  const name = user?.username ?? 'Guest';

  return (
    <Layout>
      <div className="container">
        <div>Welcome to the Lexicon Lair!</div>
        <div>Welcome: {name}</div>
        <hr />
        <div><Link to="/users">Manage your Users</Link></div>
        <div><Link to="/definitions">Manage your Definitions</Link></div>
      </div>
    </Layout>
  );
}
