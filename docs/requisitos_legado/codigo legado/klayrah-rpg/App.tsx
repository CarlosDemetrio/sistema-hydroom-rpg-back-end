
import React, { useState, useEffect } from 'react';
import CharacterSheet, { CharacterSheetData, DEFAULT_SHEET } from './components/CharacterSheet.tsx';
import Gallery from './components/Gallery.tsx';
import Notes from './components/Notes.tsx';
import SheetManager from './components/SheetManager.tsx';

type Tab = 'sheet' | 'character' | 'items' | 'notes' | 'mySheets';

const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<Tab>('sheet');
  const [allSheets, setAllSheets] = useState<CharacterSheetData[]>([]);
  const [activeSheetId, setActiveSheetId] = useState<string | null>(null);

  // Load sheets from localStorage on initial render
  useEffect(() => {
    const savedSheets = localStorage.getItem('allCharacterSheets');
    const savedActiveId = localStorage.getItem('activeCharacterSheetId');
    let sheets: CharacterSheetData[] = savedSheets ? JSON.parse(savedSheets) : [];

    if (sheets.length === 0) {
      // If no sheets exist, create a new default one
      const newSheet: CharacterSheetData = {
        ...DEFAULT_SHEET,
        id: Date.now().toString(),
        character: 'Meu Primeiro Personagem'
      };
      sheets = [newSheet];
      setActiveSheetId(newSheet.id);
    } else if (savedActiveId && sheets.some(s => s.id === savedActiveId)) {
      setActiveSheetId(savedActiveId);
    } else {
      // If active ID is invalid or missing, default to the first sheet
      setActiveSheetId(sheets[0]?.id || null);
    }
    setAllSheets(sheets);
  }, []);

  // Save sheets and active ID to localStorage whenever they change
  useEffect(() => {
    if (allSheets.length > 0) {
        localStorage.setItem('allCharacterSheets', JSON.stringify(allSheets));
    } else {
        // Clear storage if the last sheet is deleted
        localStorage.removeItem('allCharacterSheets');
    }
    if (activeSheetId) {
        localStorage.setItem('activeCharacterSheetId', activeSheetId);
    } else {
        localStorage.removeItem('activeCharacterSheetId');
    }
  }, [allSheets, activeSheetId]);

  const handleDownloadPdf = () => {
    const sheetElement = document.getElementById('character-sheet-container');
    if (!sheetElement) {
        console.error("Character sheet element not found!");
        return;
    }
    const characterName = activeSheet?.character || 'character-sheet';

    const opt = {
      margin:       0.5,
      filename:     `${characterName.replace(/[^a-z0-9]/gi, '_').toLowerCase()}.pdf`,
      image:        { type: 'jpeg', quality: 0.98 },
      html2canvas:  { scale: 2, useCORS: true },
      jsPDF:        { unit: 'in', format: 'a4', orientation: 'portrait' }
    };

    // @ts-ignore
    html2pdf().from(sheetElement).set(opt).save();
  };

  const handleNewSheet = () => {
    const newSheet: CharacterSheetData = {
      ...DEFAULT_SHEET,
      id: Date.now().toString(),
    };
    setAllSheets(prev => [...prev, newSheet]);
    setActiveSheetId(newSheet.id);
    setActiveTab('sheet');
  };

  const handleUpdateSheet = (updatedSheet: CharacterSheetData) => {
    setAllSheets(prev => prev.map(sheet => sheet.id === updatedSheet.id ? updatedSheet : sheet));
  };

  const handleSelectSheet = (id: string) => {
      setActiveSheetId(id);
      setActiveTab('sheet');
  };

  const handleDeleteSheet = (id: string) => {
    if (window.confirm('Tem certeza que deseja apagar esta ficha? Essa ação não pode ser desfeita.')) {
        const newSheets = allSheets.filter(sheet => sheet.id !== id);

        // If we are deleting the currently active sheet
        if (activeSheetId === id) {
            // And if there are other sheets left, select the first one
            if (newSheets.length > 0) {
                setActiveSheetId(newSheets[0].id);
            } else {
                // If it was the last sheet, clear the active id before creating a new one
                setActiveSheetId(null);
            }
        }

        setAllSheets(newSheets);

        // If the list of sheets is now empty, create a new one.
        if (newSheets.length === 0) {
            handleNewSheet();
        }
    }
  };


  const activeSheet = allSheets.find(sheet => sheet.id === activeSheetId);

  const renderTabContent = () => {
    if (!activeSheet) {
        return (
            <div className="text-center p-8">
                <h2 className="text-xl font-bold">Nenhuma Ficha Selecionada</h2>
                <p>Crie uma nova ficha para começar.</p>
            </div>
        );
    }
    switch (activeTab) {
      case 'sheet':
        return <div id="character-sheet-container"><CharacterSheet sheet={activeSheet} setSheet={handleUpdateSheet} /></div>;
      case 'character':
        return <Gallery galleryId={`character_${activeSheet.id}`} title="Meu Personagem" />;
      case 'items':
        return <Gallery galleryId={`items_${activeSheet.id}`} title="Meus Itens" />;
      case 'notes':
        return <Notes notesId={`notes_${activeSheet.id}`} />;
      case 'mySheets':
        return <SheetManager sheets={allSheets} onSelectSheet={handleSelectSheet} onDeleteSheet={handleDeleteSheet} activeSheetId={activeSheetId} />;
      default:
        return <div id="character-sheet-container"><CharacterSheet sheet={activeSheet} setSheet={handleUpdateSheet} /></div>;
    }
  };

  const TabButton: React.FC<{ tab: Tab; label: string }> = ({ tab, label }) => (
    <button
      onClick={() => setActiveTab(tab)}
      className={`px-4 py-2 text-sm font-medium rounded-t-lg transition-colors duration-200 focus:outline-none ${
        activeTab === tab
          ? 'bg-white text-red-600 border-b-2 border-red-600'
          : 'text-gray-600 hover:text-black hover:bg-gray-200'
      }`}
    >
      {label}
    </button>
  );

  return (
    <div className="min-h-screen bg-gray-100 text-gray-800 p-2 sm:p-4 md:p-8 font-sans print:p-0 print:bg-white">
      <div className="max-w-7xl mx-auto">
        <header className="flex items-center justify-between mb-4 print:hidden">
          <h1 className="text-2xl md:text-4xl font-bold text-red-600 tracking-wider uppercase">
            Klayrah
          </h1>
           <div className="flex items-center gap-4">
               <button
                onClick={handleNewSheet}
                className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200 flex items-center gap-2"
               >
                 <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clipRule="evenodd" /></svg>
                 Nova Ficha
               </button>
               <button
                onClick={handleDownloadPdf}
                className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200 flex items-center gap-2"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
                Baixar Ficha como PDF
              </button>
           </div>
        </header>

        <nav className="border-b border-gray-300 print:hidden">
          <TabButton tab="sheet" label="Ficha" />
          <TabButton tab="character" label="Meu Personagem" />
          <TabButton tab="items" label="Meus Itens" />
          <TabButton tab="notes" label="Anotações" />
          <TabButton tab="mySheets" label="Minhas Fichas" />
        </nav>

        <main className="mt-4 bg-white p-4 rounded-b-lg shadow-2xl print:shadow-none print:p-0 print:mt-0 print:rounded-none">
          {renderTabContent()}
        </main>
      </div>
    </div>
  );
};

export default App;
