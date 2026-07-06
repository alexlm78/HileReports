import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiException } from '../../api/client';
import type {
  CategoryView,
  ColumnMetadata,
  DataSourceView,
  PageResponse,
  PreviewResult,
  ReportColumnView,
  ReportDefinitionView,
  ReportParameterView,
} from '../../api/types';

type Tab = 'info' | 'sql' | 'columns' | 'parameters';

interface InfoForm {
  name: string;
  description: string;
  categoryId: string;
  dataSourceId: string;
  ownerTeam: string;
  sqlText: string;
}

const EMPTY_INFO: InfoForm = {
  name: '',
  description: '',
  categoryId: '',
  dataSourceId: '',
  ownerTeam: '',
  sqlText: '',
};

const PARAM_TYPES = ['TEXT', 'NUMBER', 'DATE', 'BOOLEAN', 'LIST'] as const;
const OPERATOR_TYPES = ['EQ', 'LIKE', 'IN', 'BETWEEN', 'GT', 'LT', 'GTE', 'LTE'] as const;

export function ReportEditPage() {
  const { id } = useParams<{ id: string }>();
  const isNew = id === 'new';
  const navigate = useNavigate();
  const qc = useQueryClient();

  const [tab, setTab] = useState<Tab>('info');
  const [info, setInfo] = useState<InfoForm>(EMPTY_INFO);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [discoverResult, setDiscoverResult] = useState<ColumnMetadata[]>([]);
  const [discovering, setDiscovering] = useState(false);
  const [previewResult, setPreviewResult] = useState<PreviewResult | null>(null);
  const [previewing, setPreviewing] = useState(false);
  const [columns, setColumns] = useState<ReportColumnView[]>([]);
  const [params, setParams] = useState<ReportParameterView[]>([]);

  const { data: report } = useQuery({
    queryKey: ['admin-report', id],
    queryFn: () => api.get<ReportDefinitionView>(`/api/v1/reports/${id}`),
    enabled: !isNew,
  });

  const { data: datasourcesPage } = useQuery({
    queryKey: ['admin-datasources-list'],
    queryFn: () => api.get<PageResponse<DataSourceView>>('/api/v1/datasources?page=0&size=100'),
  });

  const { data: categoriesPage } = useQuery({
    queryKey: ['admin-categories-list'],
    queryFn: () => api.get<PageResponse<CategoryView>>('/api/v1/categories?page=0&size=100'),
  });

  useQuery({
    queryKey: ['admin-report-columns', id],
    queryFn: async () => {
      const data = await api.get<ReportColumnView[]>(`/api/v1/reports/${id}/columns`);
      setColumns(data);
      return data;
    },
    enabled: !isNew,
  });

  useQuery({
    queryKey: ['admin-report-params', id],
    queryFn: async () => {
      const data = await api.get<ReportParameterView[]>(`/api/v1/reports/${id}/parameters`);
      setParams(data);
      return data;
    },
    enabled: !isNew,
  });

  useEffect(() => {
    if (report) {
      setInfo({
        name: report.name,
        description: report.description ?? '',
        categoryId: report.categoryId ?? '',
        dataSourceId: report.dataSourceId,
        ownerTeam: '',
        sqlText: '',
      });
    }
  }, [report]);

  const datasources = datasourcesPage?.content ?? [];
  const categories = categoriesPage?.content ?? [];

  async function handleInfoSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    const body = {
      name: info.name,
      description: info.description || null,
      categoryId: info.categoryId || null,
      dataSourceId: info.dataSourceId,
      ownerTeam: info.ownerTeam || null,
      sqlText: info.sqlText || null,
    };
    try {
      if (isNew) {
        const created = await api.post<ReportDefinitionView>('/api/v1/reports', body);
        await qc.invalidateQueries({ queryKey: ['admin-reports'] });
        void navigate(`/admin/reports/${created.id}`);
      } else {
        await api.put(`/api/v1/reports/${id}`, body);
        await qc.invalidateQueries({ queryKey: ['admin-report', id] });
        await qc.invalidateQueries({ queryKey: ['admin-reports'] });
        setSuccessMsg('Saved');
      }
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleSqlSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await api.put(`/api/v1/reports/${id}`, { sqlText: info.sqlText });
      setSuccessMsg('SQL saved');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleSaveAndPreview() {
    if (!id) return;
    setPreviewing(true);
    setError(null);
    setSuccessMsg(null);
    setPreviewResult(null);
    try {
      await api.put(`/api/v1/reports/${id}`, { sqlText: info.sqlText });
      const result = await api.post<PreviewResult>(`/api/v1/reports/${id}/preview`);
      setPreviewResult(result);
      setSuccessMsg('Preview OK — report is ready to publish');
      await qc.invalidateQueries({ queryKey: ['admin-report', id] });
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Preview failed');
    } finally {
      setPreviewing(false);
    }
  }

  async function handleDiscover() {
    if (!info.dataSourceId || !info.sqlText) return;
    setDiscovering(true);
    setError(null);
    try {
      const result = await api.post<ColumnMetadata[]>(
        `/api/v1/datasources/${info.dataSourceId}/discover`,
        { sqlText: info.sqlText },
      );
      setDiscoverResult(result);
      if (columns.length === 0) {
        const mapped: ReportColumnView[] = result.map((col, i) => ({
          sourceName: col.name,
          dataType: col.type,
          label: col.label ?? col.name,
          displayType: null,
          displayFormat: null,
          ordinal: i,
          visible: true,
          sortable: true,
          filterableCandidate: false,
        }));
        setColumns(mapped);
      }
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Discover failed');
    } finally {
      setDiscovering(false);
    }
  }

  async function handleColumnsSave() {
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await api.put(`/api/v1/reports/${id}/columns`, columns);
      await qc.invalidateQueries({ queryKey: ['admin-report-columns', id] });
      setSuccessMsg('Columns saved');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleParamsSave() {
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await api.put(`/api/v1/reports/${id}/parameters`, params);
      await qc.invalidateQueries({ queryKey: ['admin-report-params', id] });
      setSuccessMsg('Parameters saved');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handlePublish() {
    try {
      await api.post(`/api/v1/reports/${id}/publish`);
      await qc.invalidateQueries({ queryKey: ['admin-report', id] });
      await qc.invalidateQueries({ queryKey: ['admin-reports'] });
      setSuccessMsg('Published');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Publish failed');
    }
  }

  async function handleUnpublish() {
    try {
      await api.post(`/api/v1/reports/${id}/unpublish`);
      await qc.invalidateQueries({ queryKey: ['admin-report', id] });
      await qc.invalidateQueries({ queryKey: ['admin-reports'] });
      setSuccessMsg('Unpublished');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Unpublish failed');
    }
  }

  function updateColumn(i: number, patch: Partial<ReportColumnView>) {
    setColumns(prev => prev.map((c, idx) => idx === i ? { ...c, ...patch } : c));
  }

  function addParam() {
    setParams(prev => [
      ...prev,
      {
        name: '',
        label: '',
        parameterType: 'TEXT',
        operatorType: 'EQ',
        required: false,
        defaultValue: null,
        allowsMultiple: false,
        sourceColumn: null,
        validationRule: null,
        position: prev.length,
      },
    ]);
  }

  function updateParam(i: number, patch: Partial<ReportParameterView>) {
    setParams(prev => prev.map((p, idx) => idx === i ? { ...p, ...patch } : p));
  }

  function removeParam(i: number) {
    setParams(prev => prev.filter((_, idx) => idx !== i));
  }

  const TABS: { key: Tab; label: string }[] = [
    { key: 'info', label: 'Info' },
    { key: 'sql', label: 'SQL' },
    { key: 'columns', label: 'Columns' },
    { key: 'parameters', label: 'Parameters' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{isNew ? 'New report' : report?.name ?? '…'}</h1>
        {!isNew && report && (
          <div className="flex gap-2">
            {report.status === 'DRAFT' ? (
              <button onClick={() => void handlePublish()}
                className="bg-green-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-green-700">
                Publish
              </button>
            ) : (
              <button onClick={() => void handleUnpublish()}
                className="bg-orange-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-orange-600">
                Unpublish
              </button>
            )}
          </div>
        )}
      </div>

      {error != null && <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>}
      {successMsg != null && <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-700">{successMsg}</div>}

      {!isNew && (
        <div className="flex border-b border-gray-200 mb-6">
          {TABS.map(t => (
            <button key={t.key} onClick={() => { setError(null); setSuccessMsg(null); setTab(t.key); }}
              className={`px-5 py-2 text-sm font-medium border-b-2 -mb-px transition-colors ${
                tab === t.key ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}>
              {t.label}
            </button>
          ))}
        </div>
      )}

      {(isNew || tab === 'info') && (
        <form onSubmit={handleInfoSave} className="space-y-4 max-w-2xl">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-xs font-medium text-gray-600 mb-1">Name</label>
              <input required value={info.name}
                onChange={e => setInfo(prev => ({ ...prev, name: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
            <div className="col-span-2">
              <label className="block text-xs font-medium text-gray-600 mb-1">Description</label>
              <textarea rows={2} value={info.description}
                onChange={e => setInfo(prev => ({ ...prev, description: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Datasource</label>
              <select required={isNew} value={info.dataSourceId}
                onChange={e => setInfo(prev => ({ ...prev, dataSourceId: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="">— select —</option>
                {datasources.map(ds => <option key={ds.id} value={ds.id}>{ds.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Category</label>
              <select value={info.categoryId}
                onChange={e => setInfo(prev => ({ ...prev, categoryId: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="">— none —</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Owner team</label>
              <input value={info.ownerTeam}
                onChange={e => setInfo(prev => ({ ...prev, ownerTeam: e.target.value }))}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>
            {isNew && (
              <div className="col-span-2">
                <label className="block text-xs font-medium text-gray-600 mb-1">SQL</label>
                <textarea rows={6} required value={info.sqlText}
                  onChange={e => setInfo(prev => ({ ...prev, sqlText: e.target.value }))}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>
            )}
          </div>
          <div className="flex justify-end">
            <button type="submit" disabled={saving}
              className="px-5 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
              {saving ? 'Saving…' : isNew ? 'Create report' : 'Save info'}
            </button>
          </div>
        </form>
      )}

      {!isNew && tab === 'sql' && (
        <div className="space-y-4 max-w-3xl">
          <form onSubmit={handleSqlSave} className="space-y-4">
            <textarea rows={12} value={info.sqlText}
              onChange={e => { setInfo(prev => ({ ...prev, sqlText: e.target.value })); setPreviewResult(null); }}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="SELECT ..." />
            <div className="flex flex-wrap items-center gap-3">
              <button type="button" onClick={() => void handleDiscover()} disabled={discovering || !info.dataSourceId}
                className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40">
                {discovering ? 'Discovering…' : 'Discover columns'}
              </button>
              {discoverResult.length > 0 && (
                <span className="text-sm text-green-600">{discoverResult.length} columns found</span>
              )}
              <button type="button" onClick={() => void handleSaveAndPreview()} disabled={previewing || !info.sqlText}
                className="px-4 py-2 border border-indigo-300 text-indigo-600 rounded-lg text-sm hover:bg-indigo-50 disabled:opacity-40">
                {previewing ? 'Running preview…' : 'Save & Preview'}
              </button>
              <button type="submit" disabled={saving}
                className="ml-auto px-5 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
                {saving ? 'Saving…' : 'Save SQL'}
              </button>
            </div>
          </form>

          {previewResult != null && (
            <div>
              <p className="text-sm text-gray-500 mb-2">{previewResult.rows.length} row(s) — preview capped by server limit</p>
              <div className="overflow-x-auto bg-white border border-gray-200 rounded-xl">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 bg-gray-50">
                      {previewResult.columns.map(col => (
                        <th key={col.name} className="px-4 py-2 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">
                          {col.label ?? col.name}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {previewResult.rows.map((row, ri) => (
                      <tr key={ri} className="border-b border-gray-100 hover:bg-gray-50">
                        {(row as unknown[]).map((cell, ci) => (
                          <td key={ci} className="px-4 py-2 text-gray-700 text-xs">
                            {cell == null ? <span className="text-gray-400">—</span> : String(cell)}
                          </td>
                        ))}
                      </tr>
                    ))}
                    {previewResult.rows.length === 0 && (
                      <tr><td colSpan={previewResult.columns.length} className="px-4 py-6 text-center text-gray-400">Query returned no rows</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {!isNew && tab === 'columns' && (
        <div className="space-y-4">
          <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 bg-gray-50">
                  {['Source', 'Type', 'Label', 'Ord', 'Visible', 'Sortable', 'Filterable'].map(h => (
                    <th key={h} className="px-3 py-2 text-left text-xs font-semibold text-gray-600 uppercase tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {columns.map((col, i) => (
                  <tr key={i} className="border-b border-gray-100">
                    <td className="px-3 py-2 text-gray-500">{col.sourceName}</td>
                    <td className="px-3 py-2 text-gray-400">{col.dataType}</td>
                    <td className="px-3 py-2">
                      <input value={col.label} onChange={e => updateColumn(i, { label: e.target.value })}
                        className="w-full border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                    </td>
                    <td className="px-3 py-2">
                      <input type="number" value={col.ordinal} onChange={e => updateColumn(i, { ordinal: parseInt(e.target.value, 10) })}
                        className="w-16 border border-gray-300 rounded px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                    </td>
                    {(['visible', 'sortable', 'filterableCandidate'] as const).map(flag => (
                      <td key={flag} className="px-3 py-2 text-center">
                        <input type="checkbox" checked={col[flag]}
                          onChange={e => updateColumn(i, { [flag]: e.target.checked })}
                          className="rounded text-indigo-600" />
                      </td>
                    ))}
                  </tr>
                ))}
                {!columns.length && (
                  <tr><td colSpan={7} className="px-4 py-6 text-center text-gray-400">No columns. Run Discover on the SQL tab first.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <div className="flex justify-end">
            <button onClick={() => void handleColumnsSave()} disabled={saving}
              className="px-5 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
              {saving ? 'Saving…' : 'Save columns'}
            </button>
          </div>
        </div>
      )}

      {!isNew && tab === 'parameters' && (
        <div className="space-y-4">
          {params.map((p, i) => (
            <div key={i} className="bg-white border border-gray-200 rounded-xl p-4 grid grid-cols-3 gap-3">
              {(['name', 'label'] as const).map(field => (
                <div key={field}>
                  <label className="block text-xs font-medium text-gray-600 mb-1 capitalize">{field}</label>
                  <input value={p[field]} onChange={e => updateParam(i, { [field]: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
              ))}
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Type</label>
                <select value={p.parameterType} onChange={e => updateParam(i, { parameterType: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                  {PARAM_TYPES.map(t => <option key={t}>{t}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Operator</label>
                <select value={p.operatorType} onChange={e => updateParam(i, { operatorType: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                  {OPERATOR_TYPES.map(t => <option key={t}>{t}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Default value</label>
                <input value={p.defaultValue ?? ''} onChange={e => updateParam(i, { defaultValue: e.target.value || null })}
                  className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Source column</label>
                <input value={p.sourceColumn ?? ''} onChange={e => updateParam(i, { sourceColumn: e.target.value || null })}
                  className="w-full border border-gray-300 rounded-lg px-2 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>
              <div className="flex items-end gap-4">
                <label className="flex items-center gap-1.5 text-sm text-gray-600">
                  <input type="checkbox" checked={p.required} onChange={e => updateParam(i, { required: e.target.checked })}
                    className="rounded text-indigo-600" />
                  Required
                </label>
                <label className="flex items-center gap-1.5 text-sm text-gray-600">
                  <input type="checkbox" checked={p.allowsMultiple} onChange={e => updateParam(i, { allowsMultiple: e.target.checked })}
                    className="rounded text-indigo-600" />
                  Multi
                </label>
                <button onClick={() => removeParam(i)} className="ml-auto text-red-500 text-sm hover:underline">Remove</button>
              </div>
            </div>
          ))}
          <div className="flex gap-3 justify-between">
            <button onClick={addParam}
              className="px-4 py-2 border border-dashed border-gray-400 rounded-lg text-sm text-gray-500 hover:border-indigo-400 hover:text-indigo-600">
              + Add parameter
            </button>
            <button onClick={() => void handleParamsSave()} disabled={saving}
              className="px-5 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50">
              {saving ? 'Saving…' : 'Save parameters'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
