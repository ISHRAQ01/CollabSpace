'use client';

import { useState } from 'react';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { Bold, Italic, List, ListOrdered, Heading1, Heading2, Undo, Redo } from 'lucide-react';

interface CollaborativeEditorProps {
  documentId: string;
  initialContent: string;
  userId: string;
  userName: string;
  onSave?: (content: string) => void;
}

export function CollaborativeEditor({
  documentId,
  initialContent,
  userId,
  userName,
  onSave,
}: CollaborativeEditorProps) {
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  
  // Create editor
  const editor = useEditor({
    extensions: [
      StarterKit,
    ],
    content: initialContent || '<p></p>',
    onUpdate: ({ editor }) => {
      const html = editor.getHTML();
      if (onSave) {
        onSave(html);
      }
      setLastSaved(new Date());
    },
  });

  if (!editor) {
    return <div className="p-6">Loading editor...</div>;
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
        
        {/* Save status */}
        <div className="ml-auto flex items-center gap-2">
          <div className="text-xs text-gray-400">
            {lastSaved ? `Saved ${lastSaved.toLocaleTimeString()}` : 'Ready'}
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
