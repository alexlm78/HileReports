import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth/AuthContext';
import { AdminLayout } from './components/AdminLayout';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { CatalogPage } from './pages/CatalogPage';
import { LoginPage } from './pages/LoginPage';
import { ReportPage } from './pages/ReportPage';
import { CategoriesPage } from './pages/admin/CategoriesPage';
import { DatasourcesPage } from './pages/admin/DatasourcesPage';
import { ReportEditPage } from './pages/admin/ReportEditPage';
import { ReportsAdminPage } from './pages/admin/ReportsAdminPage';
import { TagsPage } from './pages/admin/TagsPage';
import { UsersPage } from './pages/admin/UsersPage';

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { token, roles } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  if (!roles.includes('PLATFORM_ADMIN')) return <Navigate to="/catalog" replace />;
  return <>{children}</>;
}

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
          <Route
            path="/admin"
            element={
              <AdminRoute>
                <AdminLayout />
              </AdminRoute>
            }
          >
            <Route index element={<Navigate to="/admin/reports" replace />} />
            <Route path="reports" element={<ReportsAdminPage />} />
            <Route path="reports/:id" element={<ReportEditPage />} />
            <Route path="datasources" element={<DatasourcesPage />} />
            <Route path="users" element={<UsersPage />} />
            <Route path="categories" element={<CategoriesPage />} />
            <Route path="tags" element={<TagsPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/catalog" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
