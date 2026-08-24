'use client';

import { useEffect, useState } from 'react';
import { documentAPI } from '@/lib/api';
import { Dialog } from '@/components/ui/Dialog';
import { Button } from '@/components/ui/Button';
import { History, RotateCcw, Clock, User } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';

interface Version {
  id: string;
  versionNumber: number;
  content: string;
  createdAt: string;
  createdBy: string;
  changeSummary: string;
}

interface HistoryDialogProps {
  documentId: string;
  onClose: () => void;
  onRestore: () => void;
}

export function HistoryDialog({ documentId, onClose, onRestore }: HistoryDialogProps) {
  const [versions, setVersions] = useState<Version[]>([]);
  const [loading, setLoading] = useState(true);
  const [restoring, setRestoring] = useState<string | null>(null);

  useEffect(() => {
    loadVersions();
  }, [documentId]);

  const loadVersions = async () => {
    try {
      const response = await documentAPI.listVersions(documentId);
      setVersions(response.data);
    } catch (error) {
      console.error('Failed to load versions:', error);
    } finally {
      setLoading(false);
    }
  };

  const restoreVersion = async (versionId: string) => {
    if (!confirm('Restore this version? Current changes will be saved as a new version.')) return;
    
    setRestoring(versionId);
    try {
      await documentAPI.restoreVersion(documentId, versionId);
      onRestore();
      onClose();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to restore version');
    } finally {
      setRestoring(null);
    }
  };

  return (
    <Dialog open={true} onClose={onClose} title="Version History" maxWidth="sm:max-w-2xl">
      {loading ? (
        <div className="text-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        </div>
      ) : versions.length === 0 ? (
        <div className="text-center py-12">
          <History className="h-12 w-12 text-gray-400 mx-auto mb-2" />
          <p className="text-gray-500">No versions yet</p>
        </div>
      ) : (
        <div className="space-y-4">
          {versions.map((version) => (
            <div
              key={version.id}
              className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
            >
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-medium text-gray-900">
                    Version {version.versionNumber}
                  </span>
                  {version.versionNumber === 1 && (
                    <span className="text-xs bg-blue-100 text-blue-600 px-2 py-0.5 rounded">
                      Initial
                    </span>
                  )}
                </div>
                <p className="text-sm text-gray-600 mb-2">
                  {version.changeSummary || 'No summary'}
                </p>
                <div className="flex items-center gap-4 text-xs text-gray-400">
                  <div className="flex items-center gap-1">
                    <User className="h-3 w-3" />
                    {version.createdBy}
                  </div>
                  <div className="flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    {formatDistanceToNow(new Date(version.createdAt), { addSuffix: true })}
                  </div>
                </div>
              </div>
              
              <Button
                variant="secondary"
                size="sm"
                onClick={() => restoreVersion(version.id)}
                loading={restoring === version.id}
              >
                <RotateCcw className="h-4 w-4 mr-2" />
                Restore
              </Button>
            </div>
          ))}
        </div>
      )}
    </Dialog>
  );
}
