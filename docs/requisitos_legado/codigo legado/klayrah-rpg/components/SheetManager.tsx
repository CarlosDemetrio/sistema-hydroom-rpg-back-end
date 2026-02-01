
import React from 'react';
import { CharacterSheetData } from './CharacterSheet.tsx';

interface SheetManagerProps {
    sheets: CharacterSheetData[];
    activeSheetId: string | null;
    onSelectSheet: (id: string) => void;
    onDeleteSheet: (id: string) => void;
}

const SheetManager: React.FC<SheetManagerProps> = ({ sheets, activeSheetId, onSelectSheet, onDeleteSheet }) => {
    return (
        <div>
            <h2 className="text-2xl font-bold text-red-600 mb-4">Minhas Fichas</h2>

            {sheets.length === 0 ? (
                <p className="text-gray-500">Nenhuma ficha encontrada. Clique em "Nova Ficha" para criar uma.</p>
            ) : (
                <div className="space-y-3">
                    {sheets.map(sheet => (
                        <div
                            key={sheet.id}
                            className={`flex items-center justify-between p-4 rounded-lg border transition-all duration-200 ${
                                activeSheetId === sheet.id 
                                    ? 'bg-red-50 border-red-500 shadow-md' 
                                    : 'bg-gray-50 border-gray-200 hover:shadow-lg hover:border-red-300'
                            }`}
                        >
                            <div className="cursor-pointer flex-grow" onClick={() => onSelectSheet(sheet.id)}>
                                <h3 className="font-bold text-lg text-gray-800">{sheet.character || 'Personagem Sem Nome'}</h3>
                                <p className="text-sm text-gray-500">Jogador: {sheet.player || 'N/A'}</p>
                            </div>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation(); // Prevent selection when deleting
                                    onDeleteSheet(sheet.id);
                                }}
                                className="ml-4 bg-gray-200 hover:bg-red-500 hover:text-white text-gray-600 font-bold py-2 px-3 rounded-full transition-colors duration-200"
                                aria-label={`Apagar ficha de ${sheet.character}`}
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                    <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm4 0a1 1 0 012 0v6a1 1 0 11-2 0V8z" clipRule="evenodd" />
                                </svg>
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default SheetManager;
