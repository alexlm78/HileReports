import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiException } from '../../api/client';
import type { TagView } from '../../api/types';

export function TagsPage() {
  const qc = useQueryClient();
  const [name, setName] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { data: tags = [] } = useQuery({
    queryKey: ['admin-tags'],
    queryFn: () => api.get<TagView[]>('/api/v1/tags'),
  });

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;
    setSaving(true);
    setError(null);
    try {
      await api.post('/api/v1/tags', { name: name.trim() });
      await qc.invalidateQueries({ queryKey: ['admin-tags'] });
      setName('');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Create failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this tag?')) return;
    try {
      await api.delete(`/api/v1/tags/${id}`);
      await qc.invalidateQueries({ queryKey: ['admin-tags'] });
    } catch (err) {
      alert(err instanceof ApiException ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Tags</h1>

      <form onSubmit={handleCreate} className="flex gap-3 mb-6 max-w-md">
        <input
          value={name}
          onChange={e => setName(e.target.value)}
          placeholder="Tag name"
          required
          className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <button
          type="submit"
          disabled={saving}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
        >
          {saving ? 'Creating…' : 'Create'}
        </button>
      </form>
      {error != null && <p className="text-sm text-red-600 mb-4">{error}</p>}

      <div className="flex flex-wrap gap-2">
        {tags.map(tag => (
          <div
            key={tag.id}
            className="flex items-center gap-2 px-3 py-1.5 bg-white border border-gray-200 rounded-full text-sm"
          >
            <span className="text-gray-800 font-medium">{tag.name}</span>
            <span className="text-gray-400 text-xs">#{tag.slug}</span>
            <button
              onClick={() => void handleDelete(tag.id)}
              className="text-gray-400 hover:text-red-500 transition-colors ml-1 leading-none"
              title="Delete"
            >
              ×
            </button>
          </div>
        ))}
        {tags.length === 0 && <p className="text-sm text-gray-400">No tags yet.</p>}
      </div>
    </div>
  );
}
