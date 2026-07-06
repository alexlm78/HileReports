import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { api, ApiException } from '../api/client';
import type {
  ExecutionResultView,
  ExportJobView,
  ReportDefinitionView,
  ReportParameter,
} from '../api/types';

const TERMINAL = new Set(['COMPLETED', 'FAILED', 'EXPIRED']);

async function downloadExport(exportId: string, format: string) {
  const token = localStorage.getItem('jwt_token');
  const res = await fetch(`/api/v1/exports/${exportId}/download`, {
    headers: { Authorization: `Bearer ${token ?? ''}` },
  });
  if (!res.ok) throw new Error(`Download failed: ${res.status}`);
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `export.${format.toLowerCase()}`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export function ReportPage() {
  const { id } = useParams<{ id: string }>();
  const [paramValues, setParamValues] = useState<Record<string, string>>({});
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<ExecutionResultView | null>(null);
  const [executing, setExecuting] = useState(false);
  const [execError, setExecError] = useState<string | null>(null);

  const [exportJobId, setExportJobId] = useState<string | null>(null);
  const [exporting, setExporting] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);

  const { data: report, isLoading: loadingReport } = useQuery({
    queryKey: ['report', id],
    queryFn: () => api.get<ReportDefinitionView>(`/api/v1/reports/${id}`),
    enabled: id != null,
  });

  const { data: params } = useQuery({
    queryKey: ['report-params', id],
    queryFn: () => api.get<ReportParameter[]>(`/api/v1/reports/${id}/parameters`),
    enabled: id != null,
  });

  const { data: exportJob } = useQuery({
    queryKey: ['export-job', exportJobId],
    queryFn: () => api.get<ExportJobView>(`/api/v1/exports/${exportJobId}`),
    enabled: exportJobId != null,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status == null || !TERMINAL.has(status) ? 2000 : false;
    },
  });

  async function runReport(targetPage: number) {
    if (id == null) return;
    setExecError(null);
    setExecuting(true);
    try {
      const res = await api.post<ExecutionResultView>(`/api/v1/reports/${id}/execute`, {
        parameterValues: paramValues,
        page: targetPage,
        pageSize: 50,
      });
      setResult(res);
      setPage(targetPage);
    } catch (err) {
      setExecError(err instanceof ApiException ? err.message : 'Execution failed');
    } finally {
      setExecuting(false);
    }
  }

  async function requestExport(format: 'CSV' | 'XLSX') {
    if (id == null) return;
    setExporting(true);
    setExportError(null);
    setExportJobId(null);
    try {
      const job = await api.post<ExportJobView>(`/api/v1/reports/${id}/export`, {
        format,
        parameters: paramValues,
        pageSize: 10000,
      });
      setExportJobId(job.id);
    } catch (err) {
      setExportError(err instanceof ApiException ? err.message : 'Export request failed');
    } finally {
      setExporting(false);
    }
  }

  async function handleDownload() {
    if (exportJob == null) return;
    setDownloading(true);
    try {
      await downloadExport(exportJob.id, exportJob.format);
    } catch (err) {
      setExportError(err instanceof Error ? err.message : 'Download failed');
    } finally {
      setDownloading(false);
    }
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    void runReport(0);
  }

  if (loadingReport) return <p className="text-gray-500">Loading…</p>;
  if (report == null) return <p className="text-red-600">Report not found.</p>;

  const hasMorePages = result != null && result.rows.length >= 50;
  const exportReady = exportJob?.status === 'COMPLETED';
  const exportFailed = exportJob?.status === 'FAILED' || exportJob?.status === 'EXPIRED';
  const exportRunning = exportJobId != null && !TERMINAL.has(exportJob?.status ?? '');

  return (
    <div>
      <div className="mb-6">
        <Link to="/catalog" className="text-sm text-indigo-600 hover:underline">
          ← Catalog
        </Link>
        <h1 className="text-2xl font-bold text-gray-900 mt-2">{report.name}</h1>
        {report.description != null && (
          <p className="text-gray-500 mt-1">{report.description}</p>
        )}
      </div>

      <form
        onSubmit={handleSubmit}
        className="bg-white border border-gray-200 rounded-xl p-5 mb-6"
      >
        {params != null && params.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
            {params.map(p => (
              <div key={p.id}>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {p.label}
                  {p.required && <span className="text-red-500 ml-1">*</span>}
                </label>
                <input
                  type="text"
                  value={paramValues[p.name] ?? p.defaultValue ?? ''}
                  onChange={e =>
                    setParamValues(prev => ({ ...prev, [p.name]: e.target.value }))
                  }
                  required={p.required}
                  placeholder={p.defaultValue ?? undefined}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            ))}
          </div>
        )}

        <div className="flex flex-wrap items-center gap-3">
          <button
            type="submit"
            disabled={executing}
            className="bg-indigo-600 text-white rounded-lg px-5 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
          >
            {executing ? 'Running…' : 'Run report'}
          </button>

          <div className="flex items-center gap-2">
            <button
              type="button"
              disabled={exporting || exportRunning}
              onClick={() => void requestExport('CSV')}
              className="border border-gray-300 rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 transition-colors"
            >
              Export CSV
            </button>
            <button
              type="button"
              disabled={exporting || exportRunning}
              onClick={() => void requestExport('XLSX')}
              className="border border-gray-300 rounded-lg px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 transition-colors"
            >
              Export XLSX
            </button>
          </div>

          {exportRunning && (
            <span className="text-sm text-gray-500 animate-pulse">Generating export…</span>
          )}
          {exportReady && (
            <button
              type="button"
              onClick={() => void handleDownload()}
              disabled={downloading}
              className="bg-green-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-green-700 disabled:opacity-50 transition-colors"
            >
              {downloading ? 'Downloading…' : `Download ${exportJob?.format ?? ''}`}
            </button>
          )}
          {exportFailed && (
            <span className="text-sm text-red-600">Export failed or expired</span>
          )}
          {exportError != null && (
            <span className="text-sm text-red-600">{exportError}</span>
          )}
        </div>
      </form>

      {execError != null && <p className="text-red-600 mb-4">{execError}</p>}

      {result != null && (
        <div>
          <p className="text-sm text-gray-500 mb-2">
            {result.rowCount} row{result.rowCount !== 1 ? 's' : ''} · {result.durationMs}ms
          </p>
          <div className="overflow-x-auto bg-white border border-gray-200 rounded-xl">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 bg-gray-50">
                  {result.columns.map(col => (
                    <th
                      key={col.name}
                      className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide"
                    >
                      {col.label}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {result.rows.map((row, ri) => (
                  <tr key={ri} className="border-b border-gray-100 hover:bg-gray-50">
                    {(row as unknown[]).map((cell, ci) => (
                      <td key={ci} className="px-4 py-3 text-gray-700">
                        {cell == null ? (
                          <span className="text-gray-400">—</span>
                        ) : (
                          String(cell)
                        )}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex items-center gap-2 mt-4">
            <button
              disabled={page === 0 || executing}
              onClick={() => void runReport(page - 1)}
              className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50"
            >
              Previous
            </button>
            <span className="text-sm text-gray-500">Page {page + 1}</span>
            <button
              disabled={!hasMorePages || executing}
              onClick={() => void runReport(page + 1)}
              className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
