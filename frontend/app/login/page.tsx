'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { authAPI } from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card } from '@/components/ui/Card';
import { FileText } from 'lucide-react';
import Link from 'next/link';

const loginSchema = z.object({
  identifier: z.string().min(1, 'Email or username is required'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginForm = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginForm) => {
    setLoading(true);
    setError(null);
    
    try {
      // Clear any old auth data first
      clearAuth();
      
      console.log('Login data:', data);
      const response = await authAPI.login({
        identifier: data.identifier,
        password: data.password,
      });
      
      console.log('Login response:', response.data);
      
      // Handle different response structures
      const responseData = response.data;
      const user = responseData.user || {
        id: responseData.userId,
        email: responseData.email,
        username: responseData.username,
        fullName: responseData.fullName,
      };
      const token = responseData.token || responseData.accessToken;
      const refreshToken = responseData.refreshToken;
      
      if (!token || !refreshToken) {
        throw new Error('Invalid response from server');
      }
      
      console.log('Setting auth with user:', user);
      setAuth(user, token, refreshToken);
      router.push('/documents');
    } catch (err: any) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-blue-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <div className="flex justify-center mb-4">
            <div className="bg-blue-600 p-3 rounded-xl">
              <FileText className="h-8 w-8 text-white" />
            </div>
          </div>
          <h2 className="text-3xl font-extrabold text-gray-900">Welcome back</h2>
          <p className="mt-2 text-sm text-gray-600">Sign in to continue to DocuSync</p>
        </div>
        
        <Card className="p-6">
          <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
            {error && (
              <div className="rounded-md bg-red-50 border border-red-200 p-4">
                <p className="text-sm text-red-700">{error}</p>
              </div>
            )}
            
            <div className="space-y-4">
              <div>
                <label htmlFor="identifier" className="block text-sm font-medium text-gray-700 mb-1">
                  Email or Username
                </label>
                <Input
                  id="identifier"
                  type="text"
                  placeholder="you@example.com"
                  {...register('identifier')}
                  error={errors.identifier?.message}
                />
              </div>
              
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                  Password
                </label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  {...register('password')}
                  error={errors.password?.message}
                />
              </div>
            </div>
            
            <Button type="submit" loading={loading} className="w-full">
              Sign in
            </Button>
          </form>
        </Card>
        
        <p className="text-center text-sm text-gray-600">
          Don't have an account?{' '}
          <Link href="/register" className="font-medium text-blue-600 hover:text-blue-500">
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}
