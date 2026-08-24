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

const registerSchema = z.object({
  fullName: z.string().min(2, 'Full name is required'),
  email: z.string().email('Invalid email address'),
  username: z.string().min(3, 'Username must be at least 3 characters'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type RegisterForm = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterForm) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await authAPI.register({
        fullName: data.fullName,
        email: data.email,
        username: data.username,
        password: data.password,
      });
      
      console.log('Register response:', response.data);
      
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
      
      setAuth(user, token, refreshToken);
      router.push('/documents');
    } catch (err: any) {
      console.error('Register error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to register');
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
          <h2 className="text-3xl font-extrabold text-gray-900">Create your account</h2>
          <p className="mt-2 text-sm text-gray-600">Start collaborating in real-time</p>
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
                <label htmlFor="fullName" className="block text-sm font-medium text-gray-700 mb-1">
                  Full Name
                </label>
                <Input
                  id="fullName"
                  type="text"
                  placeholder="John Doe"
                  {...register('fullName')}
                  error={errors.fullName?.message}
                />
              </div>
              
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                  Email address
                </label>
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  {...register('email')}
                  error={errors.email?.message}
                />
              </div>
              
              <div>
                <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
                  Username
                </label>
                <Input
                  id="username"
                  type="text"
                  placeholder="johndoe"
                  {...register('username')}
                  error={errors.username?.message}
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
              
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
                  Confirm Password
                </label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  {...register('confirmPassword')}
                  error={errors.confirmPassword?.message}
                />
              </div>
            </div>
            
            <Button type="submit" loading={loading} className="w-full">
              Create account
            </Button>
          </form>
        </Card>
        
        <p className="text-center text-sm text-gray-600">
          Already have an account?{' '}
          <Link href="/login" className="font-medium text-blue-600 hover:text-blue-500">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
