
import React, { useState } from 'react';

const SectionTitle: React.FC<{ children: React.ReactNode; className?: string }> = ({ children, className = '' }) => (
  <h2 className={`bg-black text-white text-center font-bold py-1 uppercase tracking-wider ${className}`}>
    {children}
  </h2>
);

const DICE_SIDES = [3, 4, 6, 8, 10, 12];

const Prospeccao: React.FC = () => {
    const [selectedDie, setSelectedDie] = useState(12);
    const [count, setCount] = useState(0);

    const handleDieSelect = (sides: number) => {
        setSelectedDie(sides);
        setCount(0); // Reset count when die changes
    };

    const handleCountChange = (amount: number) => {
        setCount(prev => {
            const newValue = prev + amount;
            if (newValue < 0) return 0;
            if (newValue > selectedDie) return selectedDie;
            return newValue;
        });
    };

    return (
        <div className="print-hidden">
            <SectionTitle>Prospecção</SectionTitle>
            <div className="p-3 border border-black bg-white space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-500 mb-1">Dado de Prospecção</label>
                    <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
                        {DICE_SIDES.map(s => (
                            <button
                                key={s}
                                onClick={() => handleDieSelect(s)}
                                className={`py-1 rounded text-sm font-bold transition-colors duration-200 ${
                                    selectedDie === s
                                        ? 'bg-red-600 text-white ring-2 ring-red-400'
                                        : 'bg-gray-200 hover:bg-gray-300 text-gray-800'
                                }`}
                            >
                                d{s}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="text-center bg-gray-50 p-4 rounded-lg border border-gray-200">
                    <p className="text-gray-500 text-sm">Contador (Max: {selectedDie})</p>
                    <div className="flex items-center justify-center gap-4 my-2">
                         <button
                            onClick={() => handleCountChange(-1)}
                            className="px-4 py-2 bg-gray-300 text-black rounded-full font-bold text-lg hover:bg-gray-400 disabled:opacity-50"
                            disabled={count === 0}
                         >
                            -
                         </button>
                         <p className="text-5xl font-bold text-red-600 w-24 text-center">{count}</p>
                          <button
                            onClick={() => handleCountChange(1)}
                            className="px-4 py-2 bg-gray-300 text-black rounded-full font-bold text-lg hover:bg-gray-400 disabled:opacity-50"
                            disabled={count === selectedDie}
                         >
                            +
                         </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Prospeccao;
