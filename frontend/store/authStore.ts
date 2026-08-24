import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: string;
  email: string;
  username: string;
  fullName: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  setAuth: (user: User, token: string, refreshToken: string) => void;
  setTokens: (token: string, refreshToken: string) => void;
  logout: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      
      setAuth: (user, token, refreshToken) => {
        console.log('Setting auth with user:', user);
        set({
          user,
          token,
          refreshToken,
          isAuthenticated: true,
        });
      },
      
      setTokens: (token, refreshToken) =>
        set({
          token,
          refreshToken,
        }),
      
      logout: () => {
        console.log('Logging out');
        set({
          user: null,
          token: null,
          refreshToken: null,
          isAuthenticated: false,
        });
      },
      
      clearAuth: () => {
        console.log('Clearing auth');
        set({
          user: null,
          token: null,
          refreshToken: null,
          isAuthenticated: false,
        });
        localStorage.removeItem('docusync-auth');
      },
    }),
    {
      name: 'docusync-auth',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
