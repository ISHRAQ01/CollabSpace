'use client';

import { useEffect, useState } from 'react';
import { documentAPI } from '@/lib/api';
import { Dialog } from '@/components/ui/Dialog';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Users, UserPlus, X, Crown, Shield } from 'lucide-react';

interface Collaborator {
  id: string;
  email: string;
  username: string;
  fullName: string;
  role: 'OWNER' | 'EDITOR' | 'VIEWER';
  joinedAt: string;
}

interface CollaboratorsDialogProps {
  documentId: string;
  onClose: () => void;
}

export function CollaboratorsDialog({ documentId, onClose }: CollaboratorsDialogProps) {
  const [collaborators, setCollaborators] = useState<Collaborator[]>([]);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<'EDITOR' | 'VIEWER'>('EDITOR');
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    loadCollaborators();
  }, [documentId]);

  const loadCollaborators = async () => {
    try {
      const response = await documentAPI.listCollaborators(documentId);
      setCollaborators(response.data);
    } catch (error) {
      console.error('Failed to load collaborators:', error);
    } finally {
      setLoading(false);
    }
  };

  const addCollaborator = async () => {
    if (!email.trim()) return;
    
    setAdding(true);
    try {
      await documentAPI.addCollaborator(documentId, {
        email: email.trim(),
        role,
      });
      setEmail('');
      loadCollaborators();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to add collaborator');
    } finally {
      setAdding(false);
    }
  };

  const removeCollaborator = async (userId: string) => {
    if (!confirm('Remove this collaborator?')) return;
    
    try {
      await documentAPI.removeCollaborator(documentId, userId);
      loadCollaborators();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to remove collaborator');
    }
  };

  const updateRole = async (userId: string, newRole: 'EDITOR' | 'VIEWER') => {
    try {
      await documentAPI.updateCollaborator(documentId, userId, { role: newRole });
      loadCollaborators();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to update role');
    }
  };

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'OWNER':
        return <Crown className="h-4 w-4 text-yellow-500" />;
      case 'EDITOR':
        return <Shield className="h-4 w-4 text-blue-500" />;
      default:
        return <Users className="h-4 w-4 text-gray-400" />;
    }
  };

  return (
    <Dialog open={true} onClose={onClose} title="Collaborators" maxWidth="sm:max-w-2xl">
      {/* Add Collaborator */}
      <div className="mb-6">
        <h4 className="text-sm font-medium text-gray-700 mb-2">Add Collaborator</h4>
        <div className="flex gap-2">
          <Input
            type="email"
            placeholder="Enter email address..."
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && addCollaborator()}
          />
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as 'EDITOR' | 'VIEWER')}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="EDITOR">Editor</option>
            <option value="VIEWER">Viewer</option>
          </select>
          <Button onClick={addCollaborator} loading={adding}>
            <UserPlus className="h-4 w-4 mr-2" />
            Add
          </Button>
        </div>
      </div>

      {/* Collaborators List */}
      <div className="space-y-3">
        {loading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
          </div>
        ) : collaborators.length === 0 ? (
          <div className="text-center py-8">
            <Users className="h-12 w-12 text-gray-400 mx-auto mb-2" />
            <p className="text-gray-500">No collaborators yet</p>
          </div>
        ) : (
          collaborators.map((collaborator) => (
            <div
              key={collaborator.id}
              className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                  <span className="text-sm font-medium text-blue-600">
                    {collaborator.fullName.charAt(0).toUpperCase()}
                  </span>
                </div>
                <div>
                  <p className="font-medium text-gray-900">{collaborator.fullName}</p>
                  <p className="text-sm text-gray-500">{collaborator.email}</p>
                </div>
              </div>
              
              <div className="flex items-center gap-2">
                {getRoleIcon(collaborator.role)}
                {collaborator.role !== 'OWNER' ? (
                  <>
                    <select
                      value={collaborator.role}
                      onChange={(e) => updateRole(collaborator.id, e.target.value as 'EDITOR' | 'VIEWER')}
                      className="text-sm border border-gray-300 rounded-md px-2 py-1 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    >
                      <option value="EDITOR">Editor</option>
                      <option value="VIEWER">Viewer</option>
                    </select>
                    <button
                      onClick={() => removeCollaborator(collaborator.id)}
                      className="text-gray-400 hover:text-red-600 transition-colors"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </>
                ) : (
                  <span className="text-sm text-gray-500">Owner</span>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </Dialog>
  );
}
