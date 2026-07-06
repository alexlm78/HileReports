import type { ApiError } from './types';

const BASE_URL = '';

export class ApiException extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = 'ApiException';
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = localStorage.getItem('jwt_token');
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(init?.headers as Record<string, string> | undefined),
  };

  const response = await fetch(`${BASE_URL}${path}`, { ...init, headers });

  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as Partial<ApiError>;
    throw new ApiException(
      response.status,
      body.code ?? 'UNKNOWN',
      body.message ?? response.statusText,
    );
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'POST',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'PUT',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
  delete: <T = void>(path: string) => request<T>(path, { method: 'DELETE' }),
};
