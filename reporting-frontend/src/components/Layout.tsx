import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout() {
  const { username, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    void navigate('/login');
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between">
        <Link to="/catalog" className="text-lg font-semibold text-indigo-600">
          HileReports
        </Link>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600">{username}</span>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-500 hover:text-gray-800 transition-colors"
          >
            Sign out
          </button>
        </div>
      </nav>
      <main className="max-w-7xl mx-auto px-6 py-8">
        <Outlet />
      </main>
    </div>
  );
}
