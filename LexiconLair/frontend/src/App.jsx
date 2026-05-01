import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import RequireAuth from './auth/RequireAuth';
import Login from './pages/Login';
import Welcome from './pages/Welcome';
import ListAuthors from './pages/authors/ListAuthors';
import AddAuthor from './pages/authors/AddAuthor';
import ListBooks from './pages/books/ListBooks';
import AddBook from './pages/books/AddBook';
import ListWords from './pages/words/ListWords';
import AddWord from './pages/words/AddWord';
import ListUsers from './pages/users/ListUsers';
import AddUser from './pages/users/AddUser';
import ListDefinitions from './pages/definitions/ListDefinitions';

// Client-side routes for the LexiconLair SPA.
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

          <Route path="/authors" element={<RequireAuth><ListAuthors /></RequireAuth>} />
          <Route path="/authors/add" element={<RequireAuth><AddAuthor /></RequireAuth>} />
          <Route path="/authors/update/:id" element={<RequireAuth><AddAuthor /></RequireAuth>} />

          <Route path="/books" element={<RequireAuth><ListBooks /></RequireAuth>} />
          <Route path="/books/add" element={<RequireAuth><AddBook /></RequireAuth>} />
          <Route path="/books/update/:id" element={<RequireAuth><AddBook /></RequireAuth>} />

          <Route path="/words" element={<RequireAuth><ListWords /></RequireAuth>} />
          <Route path="/words/add" element={<RequireAuth><AddWord /></RequireAuth>} />
          <Route path="/words/update/:id" element={<RequireAuth><AddWord /></RequireAuth>} />

          <Route path="/users" element={<RequireAuth><ListUsers /></RequireAuth>} />
          <Route path="/users/add" element={<RequireAuth><AddUser /></RequireAuth>} />
          <Route path="/users/update/:id" element={<RequireAuth><AddUser /></RequireAuth>} />

          <Route path="/definitions" element={<RequireAuth><ListDefinitions /></RequireAuth>} />

          {/* Catch-all redirects unknown paths to home */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
