export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface ApiError {
  code: string;
  message: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export interface ReportDefinitionView {
  id: string;
  name: string;
  description: string | null;
  status: 'DRAFT' | 'PUBLISHED';
  dataSourceId: string;
  categoryId: string | null;
  createdBy: string;
  createdAt: string;
}

export interface ReportParameter {
  id: string;
  reportDefinitionId: string;
  name: string;
  label: string;
  type: string;
  required: boolean;
  defaultValue: string | null;
  position: number;
}

export interface ColumnMetadata {
  name: string;
  label: string;
  type: string;
}

export interface ExecutionResultView {
  executionId: string;
  correlationId: string;
  columns: ColumnMetadata[];
  rows: unknown[][];
  rowCount: number;
  durationMs: number;
  page: number;
  pageSize: number;
}
