
import React, { useState, useEffect } from 'react';

interface Note {
  id: number;
  content: string;
  timestamp: string;
}

interface NotesProps {
    notesId: string;
}

const Notes: React.FC<NotesProps> = ({ notesId }) => {
  const [notes, setNotes] = useState<Note[]>([]);
  const [currentNote, setCurrentNote] = useState('');

  useEffect(() => {
    const savedNotes = localStorage.getItem(notesId);
    if (savedNotes) {
      setNotes(JSON.parse(savedNotes));
    } else {
      setNotes([]); // Clear notes when switching to a sheet with no notes
    }
  }, [notesId]);

  const saveNotes = (newNotes: Note[]) => {
    setNotes(newNotes);
    localStorage.setItem(notesId, JSON.stringify(newNotes));
  };

  const handleAddNote = () => {
    if (currentNote.trim() === '') return;

    const newNote: Note = {
      id: Date.now(),
      content: currentNote,
      timestamp: new Date().toLocaleString(),
    };

    saveNotes([newNote, ...notes]);
    setCurrentNote('');
  };

  const handleDeleteNote = (id: number) => {
    if (window.confirm('Are you sure you want to delete this note?')) {
      const filteredNotes = notes.filter(note => note.id !== id);
      saveNotes(filteredNotes);
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-red-600 mb-4">Anotações</h2>
      
      <div className="mb-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
        <textarea
          value={currentNote}
          onChange={(e) => setCurrentNote(e.target.value)}
          placeholder="Write your note here..."
          className="w-full h-32 p-2 bg-white border border-gray-300 rounded-md text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-500"
        />
        <button
          onClick={handleAddNote}
          className="mt-2 bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200"
        >
          Save Note
        </button>
      </div>

      <div className="space-y-4">
        {notes.map((note) => (
          <div key={note.id} className="p-4 bg-gray-50/50 rounded-lg shadow-md relative border border-gray-200">
            <p className="text-gray-700 whitespace-pre-wrap">{note.content}</p>
            <div className="text-xs text-gray-500 mt-2">{note.timestamp}</div>
            <button
              onClick={() => handleDeleteNote(note.id)}
              className="absolute top-2 right-2 text-gray-400 hover:text-red-500"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </button>
          </div>
        ))}
        {notes.length === 0 && <p className="text-gray-500">No notes yet. Add one above!</p>}
      </div>
    </div>
  );
};

export default Notes;
