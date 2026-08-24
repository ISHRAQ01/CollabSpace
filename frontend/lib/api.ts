import axios from 'axios';
import { useAuthStore } from '@/store/authStore';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        if (refreshToken) {
          const response = await axios.post(`${API_URL}/auth/refresh`, {
            refreshToken,
          });
          
          const { token, refreshToken: newRefreshToken } = response.data;
          useAuthStore.getState().setTokens(token, newRefreshToken);
          
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (data: any) => api.post('/auth/register', data),
  login: (data: any) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  refresh: (refreshToken: string) => api.post('/auth/refresh', { refreshToken }),
};

// Document API
export const documentAPI = {
  list: () => api.get('/documents'),
  create: (data: any) => api.post('/documents', data),
  get: (id: string) => api.get(`/documents/${id}`),
  update: (id: string, data: any) => api.put(`/documents/${id}`, data),
  delete: (id: string) => api.delete(`/documents/${id}`),
  
  // Collaborators
  listCollaborators: (id: string) => api.get(`/documents/${id}/collaborators`),
  addCollaborator: (id: string, data: any) => api.post(`/documents/${id}/collaborators`, data),
  updateCollaborator: (id: string, userId: string, data: any) => 
    api.put(`/documents/${id}/collaborators/${userId}`, data),
  removeCollaborator: (id: string, userId: string) => 
    api.delete(`/documents/${id}/collaborators/${userId}`),
  
  // Versions
  listVersions: (id: string) => api.get(`/documents/${id}/versions`),
  getVersion: (id: string, versionId: string) => api.get(`/documents/${id}/versions/${versionId}`),
  restoreVersion: (id: string, versionId: string) => 
    api.post(`/documents/${id}/versions/${versionId}/restore`),
};