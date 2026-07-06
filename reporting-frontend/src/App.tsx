import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { CatalogPage } from './pages/CatalogPage';
import { LoginPage } from './pages/LoginPage';
import { ReportPage } from './pages/ReportPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/catalog" element={<CatalogPage />} />
            <Route path="/reports/:id" element={<ReportPage />} />
            <Route path="/" element={<Navigate to="/catalog" replace />} />
          </Route>
          <Route path="*" element={<Navigate to="/catalog" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
