'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { documentAPI } from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Dialog } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Plus, FileText, Clock, Trash2, Search, LogOut } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

interface Document {
  id: string;
  title: string;
  content: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export default function DocumentsPage() {
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showNewDocDialog, setShowNewDocDialog] = useState(false);
  const [newDocTitle, setNewDocTitle] = useState('');
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const response = await documentAPI.list();
      console.log('Documents response:', response.data);
      
      // Handle both Page and array responses
      if (response.data.content) {
        setDocuments(response.data.content);
      } else if (Array.isArray(response.data)) {
        setDocuments(response.data);
      } else {
        setDocuments([]);
      }
    } catch (error) {
      console.error('Failed to load documents:', error);
      setDocuments([]);
    } finally {
      setLoading(false);
    }
  };

  const createDocument = async () => {
    if (!newDocTitle.trim()) return;
    
    setCreating(true);
    try {
      const response = await documentAPI.create({
        title: newDocTitle,
        content: '',
      });
      
      console.log('Created document:', response.data);
      setShowNewDocDialog(false);
      setNewDocTitle('');
      
      // Navigate to the new document
      if (response.data.id) {
        router.push(`/documents/${response.data.id}`);
      } else {
        loadDocuments();
      }
    } catch (error: any) {
      console.error('Failed to create document:', error);
      alert(error.response?.data?.message || 'Failed to create document');
    } finally {
      setCreating(false);
    }
  };

  const deleteDocument = async (id: string) => {
    if (!confirm('Are you sure you want to delete this document?')) return;
    
    try {
      await documentAPI.delete(id);
      setDocuments(documents.filter((doc) => doc.id !== id));
    } catch (error) {
      console.error('Failed to delete document:', error);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const filteredDocuments = documents.filter((doc) =>
    doc.title?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">My Documents</h1>
              <p className="mt-1 text-sm text-gray-500">
                Welcome back, {user?.fullName}!
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Button onClick={() => setShowNewDocDialog(true)}>
                <Plus className="h-5 w-5 mr-2" />
                New Document
              </Button>
              <Button variant="ghost" onClick={handleLogout}>
                <LogOut className="h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {/* Search */}
        <div className="mb-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search documents..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>

        {/* Documents Grid */}
        {loading ? (
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          </div>
        ) : filteredDocuments.length === 0 ? (
          <div className="text-center py-12">
            <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900">No documents found</h3>
            <p className="mt-2 text-sm text-gray-500">
              Create your first document to get started!
            </p>
            <Button className="mt-4" onClick={() => setShowNewDocDialog(true)}>
              <Plus className="h-5 w-5 mr-2" />
              Create Document
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {filteredDocuments.map((doc) => (
              <Card
                key={doc.id}
                className="hover:shadow-lg transition-shadow cursor-pointer group"
                onClick={() => router.push(`/documents/${doc.id}`)}
              >
                <div className="p-6">
                  <div className="flex justify-between items-start mb-4">
                    <FileText className="h-8 w-8 text-blue-600" />
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        deleteDocument(doc.id);
                      }}
                      className="text-gray-400 hover:text-red-600 transition-colors"
                    >
                      <Trash2 className="h-5 w-5" />
                    </button>
                  </div>
                  
                  <h3 className="text-lg font-semibold text-gray-900 mb-2 group-hover:text-blue-600 transition-colors">
                    {doc.title}
                  </h3>
                  
                  <p className="text-sm text-gray-500 line-clamp-3 mb-4">
                    {doc.content || 'Empty document'}
                  </p>
                  
                  <div className="flex items-center justify-between text-xs text-gray-400">
                    <div className="flex items-center gap-2">
                      <Clock className="h-4 w-4" />
                      {doc.updatedAt && formatDistanceToNow(new Date(doc.updatedAt), { addSuffix: true })}
                    </div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </main>

      {/* New Document Dialog */}
      <Dialog
        open={showNewDocDialog}
        onClose={() => setShowNewDocDialog(false)}
        title="Create New Document"
      >
        <Input
          type="text"
          placeholder="Document title..."
          value={newDocTitle}
          onChange={(e) => setNewDocTitle(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && createDocument()}
          autoFocus
        />
        <div className="flex justify-end gap-3 mt-4">
          <Button variant="secondary" onClick={() => setShowNewDocDialog(false)}>
            Cancel
          </Button>
          <Button onClick={createDocument} loading={creating} disabled={!newDocTitle.trim()}>
            Create
          </Button>
        </div>
      </Dialog>
    </div>
  );
}
