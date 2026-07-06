import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiException } from '../../api/client';
import type { DataSourceView, PageResponse, ValidationResult } from '../../api/types';

const DB_TYPES = ['POSTGRESQL', 'MYSQL', 'ORACLE'] as const;

interface FormState {
  name: string;
  dbType: string;
  host: string;
  port: string;
  databaseOrService: string;
  username: string;
  password: string;
  sslMode: string;
}

const emptyForm: FormState = {
  name: '',
  dbType: 'POSTGRESQL',
  host: '',
  port: '5432',
  databaseOrService: '',
  username: '',
  password: '',
  sslMode: '',
};

function formFromDs(ds: DataSourceView): FormState {
  return {
    name: ds.name,
    dbType: ds.dbType,
    host: ds.host,
    port: String(ds.port),
    databaseOrService: ds.databaseOrService,
    username: ds.username,
    password: '',
    sslMode: ds.sslMode ?? '',
  };
}

export function DatasourcesPage() {
  const qc = useQueryClient();
  const [page, setPage] = useState(0);
  const [modal, setModal] = useState<null | 'create' | DataSourceView>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [testResult, setTestResult] = useState<Record<string, ValidationResult>>({});

  const { data } = useQuery({
    queryKey: ['admin-datasources', page],
    queryFn: () => api.get<PageResponse<DataSourceView>>(`/api/v1/datasources?page=${page}&size=20`),
  });

  function openCreate() {
    setForm(emptyForm);
    setError(null);
    setModal('create');
  }

  function openEdit(ds: DataSourceView) {
    setForm(formFromDs(ds));
    setError(null);
    setModal(ds);
  }

  function setField(k: keyof FormState, v: string) {
    setForm(prev => ({ ...prev, [k]: v }));
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    const body = {
      name: form.name,
      dbType: form.dbType,
      host: form.host,
      port: parseInt(form.port, 10),
      databaseOrService: form.databaseOrService,
      username: form.username,
      password: form.password || undefined,
      sslMode: form.sslMode || undefined,
    };
    try {
      if (modal === 'create') {
        await api.post('/api/v1/datasources', body);
      } else {
        await api.put(`/api/v1/datasources/${(modal as DataSourceView).id}`, body);
      }
      await qc.invalidateQueries({ queryKey: ['admin-datasources'] });
      setModal(null);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this datasource?')) return;
    try {
      await api.delete(`/api/v1/datasources/${id}`);
      await qc.invalidateQueries({ queryKey: ['admin-datasources'] });
    } catch (err) {
      alert(err instanceof ApiException ? err.message : 'Delete failed');
    }
  }

  async function handleTest(id: string) {
    try {
      const result = await api.post<ValidationResult>(`/api/v1/datasources/${id}/test`);
      setTestResult(prev => ({ ...prev, [id]: result }));
    } catch (err) {
      setTestResult(prev => ({
        ...prev,
        [id]: { valid: false, message: err instanceof ApiException ? err.message : 'Test failed' },
      }));
    }
  }

  const items = data?.content ?? [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Datasources</h1>
        <button
          onClick={openCreate}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          + New datasource
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200 bg-gray-50">
              {['Name', 'Type', 'Host', 'Database', 'Status', 'Actions'].map(h => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map(ds => (
              <tr key={ds.id} className="border-b border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{ds.name}</td>
                <td className="px-4 py-3 text-gray-500">{ds.dbType}</td>
                <td className="px-4 py-3 text-gray-500">{ds.host}:{ds.port}</td>
                <td className="px-4 py-3 text-gray-500">{ds.databaseOrService}</td>
                <td className="px-4 py-3">
                  {testResult[ds.id] != null ? (
                    <span className={testResult[ds.id].valid ? 'text-green-600' : 'text-red-600'}>
                      {testResult[ds.id].valid ? '✓ OK' : `✗ ${testResult[ds.id].message ?? 'Failed'}`}
                    </span>
                  ) : (
                    <span className="text-gray-400">{ds.status}</span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-3">
                    <button onClick={() => void handleTest(ds.id)} className="text-blue-600 hover:underline">
                      Test
                    </button>
                    <button onClick={() => openEdit(ds)} className="text-indigo-600 hover:underline">
                      Edit
                    </button>
                    <button onClick={() => void handleDelete(ds.id)} className="text-red-500 hover:underline">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {!items.length && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  No datasources configured.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex gap-2 mt-4">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
            className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40">
            Previous
          </button>
          <span className="text-sm text-gray-500 self-center">Page {page + 1} of {data.totalPages}</span>
          <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}
            className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40">
            Next
          </button>
        </div>
      )}

      {modal != null && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">
              {modal === 'create' ? 'New datasource' : `Edit ${(modal as DataSourceView).name}`}
            </h2>
            <form onSubmit={handleSave} className="space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div className="col-span-2">
                  <label className="block text-xs font-medium text-gray-600 mb-1">Name</label>
                  <input required value={form.name} onChange={e => setField('name', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Type</label>
                  <select value={form.dbType} onChange={e => setField('dbType', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                    {DB_TYPES.map(t => <option key={t}>{t}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Port</label>
                  <input required type="number" value={form.port} onChange={e => setField('port', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Host</label>
                  <input required value={form.host} onChange={e => setField('host', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Database / Service</label>
                  <input required value={form.databaseOrService} onChange={e => setField('databaseOrService', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Username</label>
                  <input required value={form.username} onChange={e => setField('username', e.target.value)}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    Password {modal !== 'create' && <span className="text-gray-400">(leave blank to keep)</span>}
                  </label>
                  <input type="password" value={form.password} onChange={e => setField('password', e.target.value)}
                    required={modal === 'create'}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
              </div>
              {error != null && <p className="text-sm text-red-600">{error}</p>}
              <div className="flex justify-end gap-3 pt-2">
                <button type="button" onClick={() => setModal(null)}
                  className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">
                  Cancel
                </button>
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
