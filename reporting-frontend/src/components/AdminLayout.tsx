import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const navItems = [
  { to: '/admin/reports', label: 'Reports', adminOnly: false },
  { to: '/admin/datasources', label: 'Datasources', adminOnly: true },
  { to: '/admin/users', label: 'Users', adminOnly: true },
  { to: '/admin/categories', label: 'Categories', adminOnly: true },
  { to: '/admin/tags', label: 'Tags', adminOnly: true },
  { to: '/admin/audit', label: 'Audit log', adminOnly: true },
];

export function AdminLayout() {
  const { username, roles, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = roles.includes('PLATFORM_ADMIN');
  const visibleItems = navItems.filter(item => !item.adminOnly || isAdmin);

  function handleLogout() {
    logout();
    void navigate('/login');
  }

  return (
    <div className="min-h-screen flex bg-gray-50">
      <aside className="w-56 bg-white border-r border-gray-200 flex flex-col">
        <div className="px-5 py-4 border-b border-gray-200">
          <NavLink to="/catalog" className="text-base font-semibold text-indigo-600">
            HileReports
          </NavLink>
          <p className="text-xs text-gray-400 mt-0.5">{isAdmin ? 'Admin' : 'Report Designer'}</p>
        </div>
        <nav className="flex-1 px-3 py-4 space-y-1">
          {visibleItems.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `block px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-indigo-50 text-indigo-700'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="px-5 py-4 border-t border-gray-200">
          <NavLink
            to="/catalog"
            className="block text-sm text-indigo-600 hover:underline mb-3"
          >
            ← Catalog
          </NavLink>
          <p className="text-sm text-gray-600 mb-2">{username}</p>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-400 hover:text-gray-700 transition-colors"
          >
            Sign out
          </button>
        </div>
      </aside>
      <main className="flex-1 px-8 py-8 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
