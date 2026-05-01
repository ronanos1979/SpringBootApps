import { useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

// JSP equivalent: login.jsp
// Key React concepts here:
//   - useState for controlled form inputs (replaces Spring form binding)
//   - useSearchParams to read ?error / ?logout (replaces ${param.error})
//   - onSubmit handler (replaces form POST to /login)
export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const hasError = searchParams.get('error') !== null;
  const hasLogout = searchParams.get('logout') !== null;

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      await login(username, password);
      navigate(location.state?.from?.pathname ?? '/', { replace: true });
    } catch (err) {
      setError(err.message || 'Invalid username or password.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="container mt-4">
      <h1>Welcome to the Login Page</h1>

      {/* <c:if test="${param.error != null}"> becomes a JS && expression */}
      {hasError && <div className="alert alert-danger">Invalid username or password.</div>}
      {hasLogout && <div className="alert alert-success">You have been logged out.</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          {/* htmlFor instead of for — JSX uses camelCase DOM attributes */}
          <label htmlFor="username" className="form-label">Username</label>
          <input
            id="username"
            type="text"
            className="form-control"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="mb-3">
          <label htmlFor="password" className="form-label">Password</label>
          <input
            id="password"
            type="password"
            className="form-control"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
        </div>
        {/* No CSRF token needed — React apps use other auth strategies (JWT, cookies) */}
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Signing in...' : 'Sign In'}
        </button>
      </form>
    </div>
  );
}
