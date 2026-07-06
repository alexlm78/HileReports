import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { api } from '../api/client';
import type { LoginRequest, LoginResponse } from '../api/types';

interface AuthContextValue {
  token: string | null;
  username: string | null;
  login: (req: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function decodeUsername(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1])) as Record<string, unknown>;
    return (payload['sub'] as string) ?? null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('jwt_token'));
  const [username, setUsername] = useState<string | null>(() => {
    const t = localStorage.getItem('jwt_token');
    return t ? decodeUsername(t) : null;
  });

  const login = useCallback(async (req: LoginRequest) => {
    const res = await api.post<LoginResponse>('/api/v1/auth/login', req);
    localStorage.setItem('jwt_token', res.token);
    setToken(res.token);
    setUsername(decodeUsername(res.token));
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('jwt_token');
    setToken(null);
    setUsername(null);
  }, []);

  const value = useMemo(
    () => ({ token, username, login, logout }),
    [token, username, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
