import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { api } from '../api/client';
import type { LoginRequest, LoginResponse } from '../api/types';

interface AuthContextValue {
  token: string | null;
  username: string | null;
  roles: string[];
  login: (req: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

interface JwtPayload {
  sub?: string;
  roles?: string[];
}

function decodePayload(token: string): JwtPayload {
  try {
    return JSON.parse(atob(token.split('.')[1])) as JwtPayload;
  } catch {
    return {};
  }
}

function decodeUsername(token: string): string | null {
  return decodePayload(token).sub ?? null;
}

function decodeRoles(token: string): string[] {
  return decodePayload(token).roles ?? [];
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('jwt_token'));
  const [username, setUsername] = useState<string | null>(() => {
    const t = localStorage.getItem('jwt_token');
    return t ? decodeUsername(t) : null;
  });
  const [roles, setRoles] = useState<string[]>(() => {
    const t = localStorage.getItem('jwt_token');
    return t ? decodeRoles(t) : [];
  });

  const login = useCallback(async (req: LoginRequest) => {
    const res = await api.post<LoginResponse>('/api/v1/auth/login', req);
    localStorage.setItem('jwt_token', res.token);
    setToken(res.token);
    setUsername(decodeUsername(res.token));
    setRoles(decodeRoles(res.token));
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('jwt_token');
    setToken(null);
    setUsername(null);
    setRoles([]);
  }, []);

  const value = useMemo(
    () => ({ token, username, roles, login, logout }),
    [token, username, roles, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
