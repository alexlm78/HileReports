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
import { AuditPage } from './pages/admin/AuditPage';
import { TagsPage } from './pages/admin/TagsPage';
import { UsersPage } from './pages/admin/UsersPage';

// Reports workspace: PLATFORM_ADMIN and REPORT_DESIGNER both build/manage reports there.
function StaffRoute({ children }: { children: React.ReactNode }) {
  const { token, roles } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  if (!roles.includes('PLATFORM_ADMIN') && !roles.includes('REPORT_DESIGNER')) {
    return <Navigate to="/catalog" replace />;
  }
  return <>{children}</>;
}

// Datasources, users, categories, tags, audit — PLATFORM_ADMIN only (matches backend SecurityConfig).
function AdminOnlyRoute({ children }: { children: React.ReactNode }) {
  const { roles } = useAuth();
  if (!roles.includes('PLATFORM_ADMIN')) return <Navigate to="/admin/reports" replace />;
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
              <StaffRoute>
                <AdminLayout />
              </StaffRoute>
            }
          >
            <Route index element={<Navigate to="/admin/reports" replace />} />
            <Route path="reports" element={<ReportsAdminPage />} />
            <Route path="reports/:id" element={<ReportEditPage />} />
            <Route
              path="datasources"
              element={
                <AdminOnlyRoute>
                  <DatasourcesPage />
                </AdminOnlyRoute>
              }
            />
            <Route
              path="users"
              element={
                <AdminOnlyRoute>
                  <UsersPage />
                </AdminOnlyRoute>
              }
            />
            <Route
              path="categories"
              element={
                <AdminOnlyRoute>
                  <CategoriesPage />
                </AdminOnlyRoute>
              }
            />
            <Route
              path="tags"
              element={
                <AdminOnlyRoute>
                  <TagsPage />
                </AdminOnlyRoute>
              }
            />
            <Route
              path="audit"
              element={
                <AdminOnlyRoute>
                  <AuditPage />
                </AdminOnlyRoute>
              }
            />
          </Route>
          <Route path="*" element={<Navigate to="/catalog" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
