'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { documentAPI } from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/ui/Button';
import { ChevronLeft, Share2, History, Clock } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { CollaborativeEditor } from '@/components/editor/CollaborativeEditor';
import { CollaboratorsDialog } from '@/components/dialogs/CollaboratorsDialog';
import { HistoryDialog } from '@/components/dialogs/HistoryDialog';

interface Document {
  id: string;
  title: string;
  content: string;
  ownerId: string;
  createdAt: string;
  updatedAt: string;
}

export default function DocumentEditorPage() {
  const params = useParams();
  const router = useRouter();
  const user = useAuthStore((state) => state.user);
  const [document, setDocument] = useState<Document | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showCollaborators, setShowCollaborators] = useState(false);
  const [showHistory, setShowHistory] = useState(false);

  useEffect(() => {
    loadDocument();
  }, [params.id]);

  const loadDocument = async () => {
    try {
      const response = await documentAPI.get(params.id as string);
      setDocument(response.data);
    } catch (error) {
      console.error('Failed to load document:', error);
      router.push('/documents');
    } finally {
      setLoading(false);
    }
  };

  const updateTitle = async (title: string) => {
    if (!document) return;
    
    setDocument({ ...document, title });
    
    try {
      await documentAPI.update(document.id, { title });
    } catch (error) {
      console.error('Failed to update title:', error);
      loadDocument();
    }
  };

  const saveContent = async (content: string) => {
    if (!document) return;
    
    setSaving(true);
    try {
      await documentAPI.update(document.id, { content });
    } catch (error) {
      console.error('Failed to save content:', error);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!document) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-4 flex-1">
            <button
              onClick={() => router.push('/documents')}
              className="text-gray-500 hover:text-gray-700 transition-colors"
            >
              <ChevronLeft className="h-6 w-6" />
            </button>
            
            <div className="flex-1">
              <input
                type="text"
                value={document.title}
                onChange={(e) => updateTitle(e.target.value)}
                className="text-lg font-semibold text-gray-900 border-none focus:ring-0 focus:outline-none bg-transparent w-full"
                placeholder="Untitled Document"
              />
              <div className="flex items-center gap-3 text-xs text-gray-500">
                <span>{user?.fullName}</span>
                <span>•</span>
                <div className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {formatDistanceToNow(new Date(document.updatedAt), { addSuffix: true })}
                </div>
                {saving && (
                  <>
                    <span>•</span>
                    <span className="text-blue-600">Saving...</span>
                  </>
                )}
              </div>
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            <Button variant="secondary" size="sm" onClick={() => setShowCollaborators(true)}>
              <Share2 className="h-4 w-4 mr-2" />
              Share
            </Button>
            <Button variant="secondary" size="sm" onClick={() => setShowHistory(true)}>
              <History className="h-4 w-4 mr-2" />
              History
            </Button>
          </div>
        </div>
      </header>

      <main className="flex-1 overflow-hidden">
        <div className="h-full max-w-5xl mx-auto p-6">
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 h-full">
            <CollaborativeEditor
              documentId={document.id}
              initialContent={document.content}
              userId={user?.id || ''}
              userName={user?.fullName || ''}
              onSave={saveContent}
            />
          </div>
        </div>
      </main>

      {showCollaborators && (
        <CollaboratorsDialog
          documentId={document.id}
          onClose={() => setShowCollaborators(false)}
        />
      )}

      {showHistory && (
        <HistoryDialog
          documentId={document.id}
          onClose={() => setShowHistory(false)}
          onRestore={loadDocument}
        />
      )}
    </div>
  );
}
