import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import RequireAuth from './auth/RequireAuth';
import Login from './pages/Login';
import Welcome from './pages/Welcome';
import ListUsers from './pages/users/ListUsers';
import AddUser from './pages/users/AddUser';
import ListDefinitions from './pages/definitions/ListDefinitions';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={(
              <RequireAuth>
                <Welcome />
              </RequireAuth>
            )}
          />

          <Route
            path="/users"
            element={(
              <RequireAuth>
                <ListUsers />
              </RequireAuth>
            )}
          />
          <Route
            path="/users/add"
            element={(
              <RequireAuth>
                <AddUser />
              </RequireAuth>
            )}
          />
          <Route
            path="/users/update/:id"
            element={(
              <RequireAuth>
                <AddUser />
              </RequireAuth>
            )}
          />

          <Route
            path="/definitions"
            element={(
              <RequireAuth>
                <ListDefinitions />
              </RequireAuth>
            )}
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
