import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../../api/client';
import type { PageResponse, ReportDefinitionView } from '../../api/types';

const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'bg-yellow-100 text-yellow-700',
  PUBLISHED: 'bg-green-100 text-green-700',
};

export function ReportsAdminPage() {
  const [page, setPage] = useState(0);

  const { data } = useQuery({
    queryKey: ['admin-reports', page],
    queryFn: () =>
      api.get<PageResponse<ReportDefinitionView>>(
        `/api/v1/reports?page=${page}&size=20&name=&status=&categoryId=`,
      ),
  });

  const items = data?.content ?? [];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Reports</h1>
        <Link
          to="/admin/reports/new"
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          + New report
        </Link>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200 bg-gray-50">
              {['Name', 'Status', 'Created by', 'Created at', 'Actions'].map(h => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map(r => (
              <tr key={r.id} className="border-b border-gray-100 hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{r.name}</td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[r.status] ?? 'bg-gray-100 text-gray-600'}`}>
                    {r.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-500">{r.createdBy}</td>
                <td className="px-4 py-3 text-gray-400">{new Date(r.createdAt).toLocaleDateString()}</td>
                <td className="px-4 py-3">
                  <Link to={`/admin/reports/${r.id}`} className="text-indigo-600 hover:underline">
                    Edit
                  </Link>
                </td>
              </tr>
            ))}
            {!items.length && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">No reports.</td></tr>
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
    </div>
  );
}
