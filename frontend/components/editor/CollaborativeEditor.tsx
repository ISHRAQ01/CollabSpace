'use client';

import { useEffect, useRef, useState } from 'react';
import * as Y from 'yjs';
import { WebsocketProvider } from 'y-websocket';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Collaboration from '@tiptap/extension-collaboration';
import CollaborationCursor from '@tiptap/extension-collaboration-cursor';
import { Bold, Italic, List, ListOrdered, Heading1, Heading2, Undo, Redo } from 'lucide-react';

interface CollaborativeEditorProps {
  documentId: string;
  initialContent: string;
  userId: string;
  userName: string;
  onSave?: (content: string) => void;
}

const colors = [
  '#958DF1', '#F98181', '#FBBC88', '#FAF594', '#70CFF8',
  '#94FADB', '#B9F18D', '#C3E2C2', '#EAECCC', '#AFC8AD',
  '#EEC759', '#9BB8CD', '#FF90BC', '#FFC0D9', '#DC8686',
  '#7ED7C1', '#F3EEEA', '#89B9AD', '#D0BFFF', '#FFF3DA',
];

const getRandomColor = () => {
  return colors[Math.floor(Math.random() * colors.length)];
};

export function CollaborativeEditor({
  documentId,
  initialContent,
  userId,
  userName,
  onSave,
}: CollaborativeEditorProps) {
  const [connected, setConnected] = useState(false);
  const [users, setUsers] = useState<any[]>([]);
  const ydocRef = useRef<Y.Doc | null>(null);
  const providerRef = useRef<WebsocketProvider | null>(null);
  const userColorRef = useRef(getRandomColor());

  // Initialize Yjs document and WebSocket connection
  useEffect(() => {
    const ydoc = new Y.Doc();
    ydocRef.current = ydoc;

    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:8080/ws';
    const provider = new WebsocketProvider(
      wsUrl,
      `document-${documentId}`,
      ydoc,
      {
        connect: true,
        params: {
          token: localStorage.getItem('token') || '',
        },
      }
    );
    providerRef.current = provider;

    provider.on('status', (event: any) => {
      setConnected(event.status === 'connected');
    });

    provider.awareness.on('change', () => {
      const states = Array.from(provider.awareness.getStates().entries());
      const activeUsers = states
        .map(([clientId, state]: [number, any]) => ({
          clientId,
          name: state.user?.name || 'Anonymous',
          color: state.user?.color || '#000000',
        }))
        .filter((user) => user.name !== 'Anonymous');
      setUsers(activeUsers);
    });

    // Set user awareness
    provider.awareness.setLocalStateField('user', {
      name: userName,
      color: userColorRef.current,
      id: userId,
    });

    return () => {
      provider.destroy();
      ydoc.destroy();
    };
  }, [documentId, userId, userName]);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        history: false,
      }),
      Collaboration.configure({
        document: ydocRef.current,
        field: 'content',
      }),
      CollaborationCursor.configure({
        provider: providerRef.current,
        user: {
          name: userName,
          color: userColorRef.current,
        },
      }),
    ],
    content: initialContent,
    onUpdate: ({ editor }) => {
      const html = editor.getHTML();
      if (onSave) {
        onSave(html);
      }
    },
  });

  if (!editor) {
    return null;
  }

  const ToolbarButton = ({ onClick, active, children }: any) => (
    <button
      onClick={onClick}
      className={`p-2 rounded hover:bg-gray-100 transition-colors ${
        active ? 'bg-gray-200' : ''
      }`}
    >
      {children}
    </button>
  );

  return (
    <div className="flex flex-col h-full">
      {/* Toolbar */}
      <div className="border-b border-gray-200 p-2 flex items-center gap-1 flex-wrap">
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleBold().run()}
          active={editor.isActive('bold')}
        >
          <Bold className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleItalic().run()}
          active={editor.isActive('italic')}
        >
          <Italic className="h-4 w-4" />
        </ToolbarButton>
        <div className="w-px h-6 bg-gray-200 mx-1" />
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          active={editor.isActive('heading', { level: 1 })}
        >
          <Heading1 className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          active={editor.isActive('heading', { level: 2 })}
        >
          <Heading2 className="h-4 w-4" />
        </ToolbarButton>
        <div className="w-px h-6 bg-gray-200 mx-1" />
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          active={editor.isActive('bulletList')}
        >
          <List className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          active={editor.isActive('orderedList')}
        >
          <ListOrdered className="h-4 w-4" />
        </ToolbarButton>
        <div className="w-px h-6 bg-gray-200 mx-1" />
        <ToolbarButton onClick={() => editor.chain().focus().undo().run()}>
          <Undo className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().redo().run()}>
          <Redo className="h-4 w-4" />
        </ToolbarButton>
        
        {/* Connection status and active users */}
        <div className="ml-auto flex items-center gap-2">
          <div className="flex -space-x-2">
            {users.map((user) => (
              <div
                key={user.clientId}
                className="w-8 h-8 rounded-full border-2 border-white flex items-center justify-center text-xs font-medium text-white"
                style={{ backgroundColor: user.color }}
                title={user.name}
              >
                {user.name.charAt(0).toUpperCase()}
              </div>
            ))}
          </div>
          <div className={`flex items-center gap-1 text-xs ${connected ? 'text-green-600' : 'text-gray-400'}`}>
            <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-gray-300'}`} />
            {connected ? 'Connected' : 'Connecting...'}
          </div>
        </div>
      </div>

      {/* Editor Content */}
      <div className="flex-1 overflow-y-auto">
        <EditorContent editor={editor} className="prose max-w-none p-6 min-h-[500px]" />
      </div>
    </div>
  );
}
