import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './useAuth';

export default function RequireAuth({ children }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="container mt-4">
        <div className="alert alert-info">Checking session...</div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}
