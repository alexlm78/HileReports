import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiException } from '../../api/client';
import type { PageResponse, UserView } from '../../api/types';

const ROLES = ['PLATFORM_ADMIN', 'REPORT_DESIGNER', 'REPORT_VIEWER'] as const;

interface CreateForm {
  username: string;
  email: string;
  password: string;
  role: string;
}

interface EditForm {
  email: string;
  role: string;
}

export function UsersPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [createModal, setCreateModal] = useState(false);
  const [editTarget, setEditTarget] = useState<UserView | null>(null);
  const [createForm, setCreateForm] = useState<CreateForm>({ username: '', email: '', password: '', role: 'REPORT_VIEWER' });
  const [editForm, setEditForm] = useState<EditForm>({ email: '', role: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { data } = useQuery({
    queryKey: ['admin-users', page],
    queryFn: () => api.get<PageResponse<UserView>>(`/api/v1/users?page=${page}&size=20`),
  });

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await api.post('/api/v1/users', createForm);
      await qc.invalidateQueries({ queryKey: ['admin-users'] });
      setCreateModal(false);
      setCreateForm({ username: '', email: '', password: '', role: 'REPORT_VIEWER' });
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Create failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleEdit(e: React.FormEvent) {
    e.preventDefault();
    if (!editTarget) return;
    setSaving(true);
    setError(null);
    try {
      await api.put(`/api/v1/users/${editTarget.id}`, editForm);
      await qc.invalidateQueries({ queryKey: ['admin-users'] });
      setEditTarget(null);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Update failed');
    } finally {
      setSaving(false);
    }
  }

  async function toggleEnabled(user: UserView) {
    try {
      if (user.enabled) {
        await api.delete(`/api/v1/users/${user.id}`);
      } else {
        await api.post(`/api/v1/users/${user.id}/enable`);
      }
      await qc.invalidateQueries({ queryKey: ['admin-users'] });
    } catch (err) {
      alert(err instanceof ApiException ? err.message : 'Action failed');
    }
  }

  const items = data?.content ?? [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Users</h1>
        <button
          onClick={() => { setError(null); setCreateModal(true); }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          + New user
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200 bg-gray-50">
              {['Username', 'Email', 'Role', 'Status', 'Actions'].map(h => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map(user => (
              <tr key={user.id} className="border-b border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{user.username}</td>
                <td className="px-4 py-3 text-gray-500">{user.email ?? '—'}</td>
                <td className="px-4 py-3 text-gray-500">{[...user.roles].join(', ')}</td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${user.enabled ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {user.enabled ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-3">
                    <button
                      onClick={() => { setEditForm({ email: user.email ?? '', role: [...user.roles][0] ?? '' }); setError(null); setEditTarget(user); }}
                      className="text-indigo-600 hover:underline"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => void toggleEnabled(user)}
                      className={user.enabled ? 'text-orange-500 hover:underline' : 'text-green-600 hover:underline'}
                    >
                      {user.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {!items.length && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">No users found.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex gap-2 mt-4">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
            className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40">Previous</button>
          <span className="text-sm text-gray-500 self-center">Page {page + 1} of {data.totalPages}</span>
          <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}
            className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40">Next</button>
        </div>
      )}

      {createModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">New user</h2>
            <form onSubmit={handleCreate} className="space-y-3">
              {(['username', 'email', 'password'] as const).map(field => (
                <div key={field}>
                  <label className="block text-xs font-medium text-gray-600 mb-1 capitalize">{field}</label>
                  <input
                    type={field === 'password' ? 'password' : 'text'}
                    required={field !== 'email'}
                    value={createForm[field]}
                    onChange={e => setCreateForm(prev => ({ ...prev, [field]: e.target.value }))}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              ))}
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Role</label>
                <select value={createForm.role} onChange={e => setCreateForm(prev => ({ ...prev, role: e.target.value }))}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                  {ROLES.map(r => <option key={r}>{r}</option>)}
                </select>
              </div>
              {error != null && <p className="text-sm text-red-600">{error}</p>}
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setCreateModal(false)}
                  className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={saving}
                  className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50">
                  {saving ? 'Creating…' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editTarget != null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Edit {editTarget.username}</h2>
            <form onSubmit={handleEdit} className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Email</label>
                <input type="email" value={editForm.email}
                  onChange={e => setEditForm(prev => ({ ...prev, email: e.target.value }))}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Role</label>
                <select value={editForm.role}
                  onChange={e => setEditForm(prev => ({ ...prev, role: e.target.value }))}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                  {ROLES.map(r => <option key={r}>{r}</option>)}
                </select>
              </div>
              {error != null && <p className="text-sm text-red-600">{error}</p>}
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setEditTarget(null)}
                  className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
                <button type="submit" disabled={saving}
                  className="px-4 py-2 text-sm bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50">
                  {saving ? 'Saving…' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
