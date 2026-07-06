import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { ReportDefinitionView } from '../api/types';

export function CatalogPage() {
  const {
    data: reports,
    isLoading,
    error,
  } = useQuery({
    queryKey: ['catalog'],
    queryFn: () => api.get<ReportDefinitionView[]>('/api/v1/catalog'),
  });

  if (isLoading) return <p className="text-gray-500">Loading reports…</p>;
  if (error != null) return <p className="text-red-600">Failed to load catalog.</p>;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Report Catalog</h1>
      {!reports?.length ? (
        <p className="text-gray-500">No published reports available.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {reports.map(report => (
            <Link
              key={report.id}
              to={`/reports/${report.id}`}
              className="bg-white border border-gray-200 rounded-xl p-5 hover:shadow-md hover:border-indigo-300 transition-all"
            >
              <h2 className="text-base font-semibold text-gray-900 mb-1">{report.name}</h2>
              {report.description != null && (
                <p className="text-sm text-gray-500 line-clamp-2">{report.description}</p>
              )}
              <p className="text-xs text-gray-400 mt-3">by {report.createdBy}</p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
