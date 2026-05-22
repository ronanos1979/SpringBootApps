import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import RequireAuth from './auth/RequireAuth';
import Login from './pages/Login';
import Welcome from './pages/Welcome';
import ListAuthors from './pages/authors/ListAuthors';
import AddAuthor from './pages/authors/AddAuthor';
import ListBooks from './pages/books/ListBooks';
import AddBook from './pages/books/AddBook';
import BookDetail from './pages/books/BookDetail';
import ListWords from './pages/words/ListWords';
import AddWord from './pages/words/AddWord';
import WordSearch from './pages/words/WordSearch';
import ListUsers from './pages/users/ListUsers';
import AddUser from './pages/users/AddUser';
import ListDefinitions from './pages/definitions/ListDefinitions';
import AdminSettings from './pages/admin/AdminSettings';
import Game from './pages/game/Game';

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

          <Route path="/authors" element={<RequireAuth role="ADMIN"><ListAuthors /></RequireAuth>} />
          <Route path="/authors/add" element={<RequireAuth role="ADMIN"><AddAuthor /></RequireAuth>} />
          <Route path="/authors/update/:id" element={<RequireAuth role="ADMIN"><AddAuthor /></RequireAuth>} />

          <Route path="/books" element={<RequireAuth role="ADMIN"><ListBooks /></RequireAuth>} />
          <Route path="/books/add" element={<RequireAuth><AddBook /></RequireAuth>} />
          <Route path="/books/update/:id" element={<RequireAuth role="ADMIN"><AddBook /></RequireAuth>} />
          <Route path="/books/:id" element={<RequireAuth role="ADMIN"><BookDetail /></RequireAuth>} />

          <Route path="/words" element={<RequireAuth role="ADMIN"><ListWords /></RequireAuth>} />
          <Route path="/words/add" element={<RequireAuth role="ADMIN"><AddWord /></RequireAuth>} />
          <Route path="/words/update/:id" element={<RequireAuth role="ADMIN"><AddWord /></RequireAuth>} />
          <Route path="/words/search" element={<RequireAuth role="ADMIN"><WordSearch /></RequireAuth>} />

          <Route path="/users" element={<RequireAuth role="ADMIN"><ListUsers /></RequireAuth>} />
          <Route path="/users/add" element={<RequireAuth role="ADMIN"><AddUser /></RequireAuth>} />
          <Route path="/users/update/:id" element={<RequireAuth role="ADMIN"><AddUser /></RequireAuth>} />

          <Route path="/definitions" element={<RequireAuth role="ADMIN"><ListDefinitions /></RequireAuth>} />

          <Route path="/admin/settings" element={<RequireAuth role="ADMIN"><AdminSettings /></RequireAuth>} />
          <Route path="/game" element={<RequireAuth><Game /></RequireAuth>} />

          {/* Catch-all redirects unknown paths to home */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
