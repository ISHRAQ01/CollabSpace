'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { FileText, Users, Zap, Shield, History, Globe } from 'lucide-react';

export default function HomePage() {
  const router = useRouter();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/documents');
    }
  }, [isAuthenticated, router]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      {/* Navigation */}
      <nav className="bg-white/80 backdrop-blur-md shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <FileText className="h-8 w-8 text-blue-600" />
              <span className="ml-2 text-xl font-bold text-gray-900">DocuSync</span>
            </div>
            <div className="flex items-center gap-4">
              <Button variant="secondary" onClick={() => router.push('/login')}>
                Sign in
              </Button>
              <Button onClick={() => router.push('/register')}>
                Get Started
              </Button>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="text-center">
          <h1 className="text-4xl tracking-tight font-extrabold text-gray-900 sm:text-5xl md:text-6xl">
            <span className="block">Collaborate in</span>
            <span className="block text-blue-600">Real-Time</span>
          </h1>
          <p className="mt-3 max-w-md mx-auto text-base text-gray-500 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
            DocuSync is an enterprise-grade collaborative workspace that enables teams to work together seamlessly with real-time editing, version control, and AI-powered insights.
          </p>
          <div className="mt-5 max-w-md mx-auto sm:flex sm:justify-center md:mt-8">
            <div className="rounded-md shadow">
              <Button
                onClick={() => router.push('/register')}
                className="w-full flex items-center justify-center px-8 py-3 text-base font-medium"
              >
                Start Free Trial
              </Button>
            </div>
            <div className="mt-3 rounded-md shadow sm:mt-0 sm:ml-3">
              <Button
                variant="secondary"
                onClick={() => router.push('/login')}
                className="w-full flex items-center justify-center px-8 py-3 text-base font-medium"
              >
                Live Demo
              </Button>
            </div>
          </div>
        </div>

        {/* Features */}
        <div className="mt-20 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <Zap className="h-8 w-8 text-blue-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Real-Time Sync</h3>
            <p className="text-gray-600">Powered by CRDT technology for conflict-free collaborative editing</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <Users className="h-8 w-8 text-blue-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Team Collaboration</h3>
            <p className="text-gray-600">Invite team members and collaborate with granular permissions</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <Shield className="h-8 w-8 text-blue-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Enterprise Security</h3>
            <p className="text-gray-600">JWT authentication and role-based access control</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <History className="h-8 w-8 text-blue-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Version History</h3>
            <p className="text-gray-600">Full version control with ability to restore previous states</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <Globe className="h-8 w-8 text-blue-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Anywhere Access</h3>
            <p className="text-gray-600">Work from any device with real-time synchronization</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <FileText className="h-8 w-8 text-blue-600 mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Rich Documents</h3>
            <p className="text-gray-600">Create beautiful documents with rich text formatting</p>
          </div>
        </div>
      </main>
    </div>
  );
}