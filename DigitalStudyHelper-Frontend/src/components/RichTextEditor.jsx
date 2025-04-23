import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Link from '@tiptap/extension-link';
import Image from '@tiptap/extension-image';
import { useState, useEffect } from 'react';
import './RichTextEditor.css';

function RichTextEditor({ groupId, initialContent = '' }) {
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);

    const editor = useEditor({
        extensions: [
            StarterKit,
            Link.configure({
                openOnClick: false,
            }),
            Image,
        ],
        content: initialContent,
        onUpdate: ({ editor }) => {
            // Debounce the save operation
            clearTimeout(saveTimeout);
            saveTimeout = setTimeout(() => {
                saveContent(editor.getHTML());
            }, 1000);
        },
    });

    let saveTimeout;

    const saveContent = async (content) => {
        setIsSaving(true);
        setError(null);

        try {
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch(`http://localhost:8080/api/notes/group/${groupId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ content })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to save note');
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setIsSaving(false);
        }
    };

    useEffect(() => {
        const fetchNote = async () => {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    throw new Error('No authentication token found');
                }

                const response = await fetch(`http://localhost:8080/api/notes/group/${groupId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch note');
                }

                const data = await response.json();
                if (editor && data.content) {
                    editor.commands.setContent(data.content);
                }
            } catch (err) {
                setError(err.message);
            }
        };

        fetchNote();
    }, [groupId, editor]);

    if (!editor) {
        return null;
    }

    return (
        <div className="rich-text-editor">
            <div className="toolbar">
                <button
                    onClick={() => editor.chain().focus().toggleBold().run()}
                    className={editor.isActive('bold') ? 'is-active' : ''}
                >
                    Bold
                </button>
                <button
                    onClick={() => editor.chain().focus().toggleItalic().run()}
                    className={editor.isActive('italic') ? 'is-active' : ''}
                >
                    Italic
                </button>
                <button
                    onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
                    className={editor.isActive('heading', { level: 1 }) ? 'is-active' : ''}
                >
                    H1
                </button>
                <button
                    onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
                    className={editor.isActive('heading', { level: 2 }) ? 'is-active' : ''}
                >
                    H2
                </button>
                <button
                    onClick={() => editor.chain().focus().toggleBulletList().run()}
                    className={editor.isActive('bulletList') ? 'is-active' : ''}
                >
                    Bullet List
                </button>
                <button
                    onClick={() => editor.chain().focus().toggleOrderedList().run()}
                    className={editor.isActive('orderedList') ? 'is-active' : ''}
                >
                    Numbered List
                </button>
                <button
                    onClick={() => {
                        const url = window.prompt('Enter URL');
                        if (url) {
                            editor.chain().focus().setLink({ href: url }).run();
                        }
                    }}
                    className={editor.isActive('link') ? 'is-active' : ''}
                >
                    Link
                </button>
            </div>
            <EditorContent editor={editor} />
            {error && <div className="error-message">{error}</div>}
            {isSaving && <div className="saving-indicator">Saving...</div>}
        </div>
    );
}

export default RichTextEditor; 