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

// Admin types

export interface DataSourceView {
  id: string;
  name: string;
  dbType: 'POSTGRESQL' | 'MYSQL' | 'ORACLE';
  host: string;
  port: number;
  databaseOrService: string;
  username: string;
  sslMode: string | null;
  status: string;
  createdBy: string;
  createdAt: string;
}

export interface CategoryView {
  id: string;
  name: string;
  description: string | null;
}

export interface ReportColumnView {
  sourceName: string;
  dataType: string;
  label: string;
  displayType: string | null;
  displayFormat: string | null;
  ordinal: number;
  visible: boolean;
  sortable: boolean;
  filterableCandidate: boolean;
}

export interface ReportParameterView {
  name: string;
  label: string;
  parameterType: string;
  operatorType: string;
  required: boolean;
  defaultValue: string | null;
  allowsMultiple: boolean;
  sourceColumn: string | null;
  validationRule: string | null;
  position: number;
}

export interface ValidationResult {
  valid: boolean;
  message: string | null;
}

export interface UserView {
  id: string;
  username: string;
  email: string | null;
  roles: string[];
  enabled: boolean;
}

export interface TagView {
  id: string;
  name: string;
  slug: string;
}

export interface PreviewResult {
  columns: ColumnMetadata[];
  rows: unknown[][];
}

export interface ExportJobView {
  id: string;
  executionId: string;
  format: 'CSV' | 'XLSX';
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'EXPIRED';
  expiresAt: string;
}
