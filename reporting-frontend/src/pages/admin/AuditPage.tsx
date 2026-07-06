import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';
import type { AuditEventView } from '../../api/types';

const STATUS_COLORS: Record<string, string> = {
  REPORT_EXECUTED: 'bg-blue-100 text-blue-700',
  REPORT_PUBLISHED: 'bg-green-100 text-green-700',
  REPORT_UNPUBLISHED: 'bg-yellow-100 text-yellow-700',
  REPORT_CREATED: 'bg-indigo-100 text-indigo-700',
  REPORT_UPDATED: 'bg-indigo-100 text-indigo-700',
  REPORT_DELETED: 'bg-red-100 text-red-700',
  USER_LOGIN: 'bg-gray-100 text-gray-600',
  USER_CREATED: 'bg-indigo-100 text-indigo-700',
  USER_UPDATED: 'bg-indigo-100 text-indigo-700',
  USER_DISABLED: 'bg-orange-100 text-orange-700',
  USER_ENABLED: 'bg-green-100 text-green-700',
  DATASOURCE_CREATED: 'bg-purple-100 text-purple-700',
  DATASOURCE_UPDATED: 'bg-purple-100 text-purple-700',
  DATASOURCE_DELETED: 'bg-red-100 text-red-700',
};

export function AuditPage() {
  const [actor, setActor] = useState('');
  const [action, setAction] = useState('');
  const [page, setPage] = useState(0);
  const limit = 50;

  const { data: events = [], isLoading } = useQuery({
    queryKey: ['audit-events', actor, action, page],
    queryFn: () => {
      const params = new URLSearchParams();
      if (actor.trim()) params.set('actor', actor.trim());
      if (action.trim()) params.set('action', action.trim());
      params.set('page', String(page));
      params.set('limit', String(limit));
      return api.get<AuditEventView[]>(`/api/v1/audit-events?${params.toString()}`);
    },
  });

  function handleFilter(e: React.FormEvent) {
    e.preventDefault();
    setPage(0);
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Audit events</h1>

      <form onSubmit={handleFilter} className="flex flex-wrap gap-3 mb-6">
        <input
          value={actor}
          onChange={e => setActor(e.target.value)}
          placeholder="Actor (username)"
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 w-48"
        />
        <input
          value={action}
          onChange={e => setAction(e.target.value)}
          placeholder="Action (e.g. REPORT_PUBLISHED)"
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 w-56"
        />
        <button type="submit"
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700">
          Filter
        </button>
        {(actor || action) && (
          <button type="button" onClick={() => { setActor(''); setAction(''); setPage(0); }}
            className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50">
            Clear
          </button>
        )}
      </form>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200 bg-gray-50">
              {['When', 'Actor', 'Action', 'Entity', 'Entity ID'].map(h => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">Loading…</td></tr>
            )}
            {!isLoading && events.map(e => (
              <tr key={e.id} className="border-b border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-400 whitespace-nowrap">
                  {new Date(e.createdAt).toLocaleString()}
                </td>
                <td className="px-4 py-3 font-medium text-gray-800">{e.actor}</td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[e.action] ?? 'bg-gray-100 text-gray-600'}`}>
                    {e.action}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-500">{e.entityType}</td>
                <td className="px-4 py-3 text-gray-400 font-mono text-xs">{e.entityId ?? '—'}</td>
              </tr>
            ))}
            {!isLoading && events.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">No events found.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="flex gap-2 mt-4">
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
          className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40">Previous</button>
        <span className="text-sm text-gray-500 self-center">Page {page + 1}</span>
        <button disabled={events.length < limit} onClick={() => setPage(p => p + 1)}
          className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40">Next</button>
      </div>
    </div>
  );
}
