import React, { useState, useCallback } from 'react';
import { GoogleGenAI } from '@google/genai';
import Prospeccao from './Prospeccao.tsx';

// --- Helper Components ---

const SectionTitle: React.FC<{ children: React.ReactNode; className?: string }> = ({ children, className = '' }) => (
  <h2 className={`bg-black text-white text-center font-bold py-1 uppercase tracking-wider ${className}`}>
    {children}
  </h2>
);

const RedSectionTitle: React.FC<{ children: React.ReactNode; className?: string }> = ({ children, className = '' }) => (
  <h2 className={`bg-red-600 text-white text-center font-bold py-1 uppercase tracking-wider ${className}`}>
    {children}
  </h2>
);

const InputField: React.FC<{ label: string; value: string | number; onChange: (e: React.ChangeEvent<HTMLInputElement>) => void; type?: string }> = ({ label, value, onChange, type = 'text' }) => (
  <div className="flex-1">
    <label className="text-xs text-gray-500 uppercase">{label}</label>
    <input
      type={type}
      value={value}
      onChange={onChange}
      className="w-full bg-gray-100 border border-gray-300 rounded px-2 py-1 text-center text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-500"
    />
  </div>
);

const SelectField: React.FC<{ label: string; value: string; onChange: (e: React.ChangeEvent<HTMLSelectElement>) => void; options: string[] }> = ({ label, value, onChange, options }) => (
    <div className="flex-1">
        <label className="text-xs text-gray-500 uppercase">{label}</label>
        <select value={value} onChange={onChange} className="w-full bg-gray-100 border border-gray-300 rounded px-2 py-1 text-center text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-500">
            {options.map(opt => <option key={opt} value={opt}>{opt}</option>)}
        </select>
    </div>
);


const ValueBox: React.FC<{ label?: string; value: string | number; small?: boolean }> = ({ label, value, small = false }) => (
    <div className="flex flex-col items-center justify-center bg-gray-100 border border-gray-300 rounded p-1 h-full">
        {label && <span className={`text-xs text-gray-500 uppercase ${small ? 'mb-0.5' : 'mb-1'}`}>{label}</span>}
        <span className={`font-bold ${small ? 'text-sm' : 'text-base'}`}>{value}</span>
    </div>
);


const AptidaoInput: React.FC<{ label: string; baseValue: number; sorteValue: number; classeValue: number; onBaseChange: (e: React.ChangeEvent<HTMLInputElement>) => void; onSorteChange: (e: React.ChangeEvent<HTMLInputElement>) => void; onClasseChange: (e: React.ChangeEvent<HTMLInputElement>) => void; }> = ({ label, baseValue, sorteValue, classeValue, onBaseChange, onSorteChange, onClasseChange }) => {
    const total = baseValue + sorteValue + classeValue;
    return (
        <div className="grid grid-cols-5 items-center gap-2">
            <label className="text-sm uppercase text-gray-600 col-span-2">{label}</label>
            <input type="number" value={baseValue} onChange={onBaseChange} className="w-full bg-gray-100 border border-gray-300 rounded px-1 py-1 text-center text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-500" />
            <input type="number" value={sorteValue} onChange={onSorteChange} className="w-full bg-gray-100 border border-gray-300 rounded px-1 py-1 text-center text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-500" />
            <input type="number" value={classeValue} onChange={onClasseChange} className="w-full bg-gray-100 border border-gray-300 rounded px-1 py-1 text-center text-gray-800 focus:outline-none focus:ring-2 focus:ring-red-500" />
            <span className="font-bold text-center text-gray-800">{total}</span>
        </div>
    );
};

const BonusField: React.FC<{
    label: string;
    baseValue: number;
    bonusData: BonusDetail;
    onBonusChange: (field: keyof BonusDetail, value: string) => void;
}> = ({ label, baseValue, bonusData, onBonusChange }) => {
    // FIX: Operator '+' cannot be applied to types 'unknown' and 'unknown'. Ensure value from reduce is a number.
    const total = baseValue + Object.values(bonusData).reduce<number>((sum, val) => sum + Number(val), 0);
    return (
        <div className="p-2 border border-gray-300 rounded-md bg-gray-50/50">
            <h4 className="font-bold text-center text-red-600 uppercase">{label}</h4>
            <div className="grid grid-cols-3 gap-x-2 gap-y-1 mt-1 text-xs text-center">
                <span className="font-semibold col-span-3">Base: {baseValue}</span>
                <InputField label="Vantagens" type="number" value={bonusData.vantagens} onChange={e => onBonusChange('vantagens', e.target.value)} />
                <InputField label="Classe" type="number" value={bonusData.classe} onChange={e => onBonusChange('classe', e.target.value)} />
                <InputField label="Itens" type="number" value={bonusData.itens} onChange={e => onBonusChange('itens', e.target.value)} />
                <InputField label="Glória" type="number" value={bonusData.gloria} onChange={e => onBonusChange('gloria', e.target.value)} />
                <InputField label="Outros" type="number" value={bonusData.outros} onChange={e => onBonusChange('outros', e.target.value)} />
                <div className="col-span-3 mt-1">
                    <ValueBox label="Total" value={total} small />
                </div>
            </div>
        </div>
    );
};


// --- Type Definitions ---
interface Attribute {
    base: number;
    nivel: number;
    outros: number;
    impeto: number; // This will now be purely for display, calculated from totals
}

interface Attributes {
    forca: Attribute;
    agilidade: Attribute;
    vigor: Attribute;
    sabedoria: Attribute;
    intuicao: Attribute;
    inteligencia: Attribute;
    astucia: Attribute;
}

interface Aptidao {
    base: number;
    sorte: number;
    classe: number;
}

interface BonusDetail {
    vantagens: number;
    classe: number;
    itens: number;
    gloria: number;
    outros: number;
}

interface Bonuses {
    bba: BonusDetail;
    bloqueio: BonusDetail;
    reflexo: BonusDetail;
    bbm: BonusDetail;
    percep: BonusDetail;
    racionc: BonusDetail;
}

interface Ameaca {
    itens: number;
    titulos: number;
    outros: number;
}

export interface CharacterSheetData {
    id: string;
    player: string;
    character: string;
    tituloHeroico: string;
    insolitus: string;
    origem: string;
    level: number;
    experience: number;
    renascimentos: number;
    indole: string;
    presenca: string;
    arquetipo: string;
    genero: string;
    classe: string;
    customClasse: string;
    descricaoFisica: {
        idade: number;
        altura: number;
        peso: number;
        cabeloCor: string;
        cabeloTamanho: string;
        olhosCor: string;
    };
    attributes: Attributes;
    characterImage: string;
    vida: { vt: number; out: number; };
    danos: { cabeça: number; tronco: number; bracoD: number; bracoE: number; pernaD: number; pernaE: number; sangue: number; };
    essencia: { renasc: number; vant: number; outros: number; gastos: number; };
    aptidoes: { [key: string]: Aptidao };
    ameaca: Ameaca;
    bonus: Bonuses;
}

const CLASSES = ['Guerreiro', 'Arqueiro', 'Monge', 'Berserker', 'Assassino', 'Fauno (Herdeiro)', 'Mago', 'Feiticeiro', 'Necromance', 'Sacerdote', 'Ladrão', 'Negociante', 'Outra...'];
const XP_LEVELS = [
    // Level 0: 0-999
    0,      // Lvl 1: 1000
    1000,   // Lvl 2: 3000
    3000,   // Lvl 3: 6000
    6000,   // Lvl 4: 10000
    10000,  // Lvl 5: 15000
    15000,  // Lvl 6: 21000
    21000,  // Lvl 7: 28000
    28000,  // Lvl 8: 36000
    36000,  // Lvl 9: 45000
    45000,  // Lvl 10: 55000
    55000,  // Lvl 11: 66000
    66000,  // Lvl 12: 78000
    78000,  // Lvl 13: 91000
    91000,  // Lvl 14: 105000
    105000, // Lvl 15: 120000
    120000, // Lvl 16: 136000
    136000, // Lvl 17: 153000
    153000, // Lvl 18: 171000
    171000, // Lvl 19: 190000
    190000, // Lvl 20: 210000
    210000, // Lvl 21: 231000
    231000, // Lvl 22: 253000
    253000, // Lvl 23: 276000
    276000, // Lvl 24: 300000
    300000, // Lvl 25: 325000
    325000, // Lvl 26: 351000
    351000, // Lvl 27: 378000
    378000, // Lvl 28: 406000
    406000, // Lvl 29: 435000
    435000, // Lvl 30: 465000
    465000, // Lvl 31: 496000
    496000, // Lvl 32: 528000
    528000, // Lvl 33: 561000
    561000, // Lvl 34: 595000
    595000  // Lvl 35
];

export const DEFAULT_SHEET: Omit<CharacterSheetData, 'id'> = {
    player: '',
    character: 'Novo Personagem',
    tituloHeroico: '',
    insolitus: '',
    origem: '',
    level: 0,
    experience: 0,
    renascimentos: 0,
    indole: 'Neutro',
    presenca: 'Neutro',
    arquetipo: '',
    genero: 'Masculino',
    classe: 'Guerreiro',
    customClasse: '',
    descricaoFisica: {
        idade: 25,
        altura: 170,
        peso: 65,
        cabeloCor: '',
        cabeloTamanho: '',
        olhosCor: '',
    },
    attributes: {
        forca: { base: 0, nivel: 0, outros: 0, impeto: 0 },
        agilidade: { base: 0, nivel: 0, outros: 0, impeto: 0 },
        vigor: { base: 0, nivel: 0, outros: 0, impeto: 0 },
        sabedoria: { base: 0, nivel: 0, outros: 0, impeto: 0 },
        intuicao: { base: 0, nivel: 0, outros: 0, impeto: 0 },
        inteligencia: { base: 0, nivel: 0, outros: 0, impeto: 0 },
        astucia: { base: 0, nivel: 0, outros: 0, impeto: 0 },
    },
    characterImage: '',
    vida: { vt: 0, out: 0 },
    danos: { cabeça: 0, tronco: 0, bracoD: 0, bracoE: 0, pernaD: 0, pernaE: 0, sangue: 0 },
    essencia: { renasc: 0, vant: 0, outros: 0, gastos: 0 },
    aptidoes: {
        acrobacia: { base: 0, sorte: 0, classe: 0 }, guarda: { base: 0, sorte: 0, classe: 0 }, aparar: { base: 0, sorte: 0, classe: 0 }, atletismo: { base: 0, sorte: 0, classe: 0 }, resvalar: { base: 0, sorte: 0, classe: 0 }, resistencia: { base: 0, sorte: 0, classe: 0 }, perseguicao: { base: 0, sorte: 0, classe: 0 }, natacao: { base: 0, sorte: 0, classe: 0 }, furtividade: { base: 0, sorte: 0, classe: 0 }, prestidigit: { base: 0, sorte: 0, classe: 0 }, conduzir: { base: 0, sorte: 0, classe: 0 }, 'arte da fuga': { base: 0, sorte: 0, classe: 0 },
        idiomas: { base: 0, sorte: 0, classe: 0 }, observacao: { base: 0, sorte: 0, classe: 0 }, falsificar: { base: 0, sorte: 0, classe: 0 }, prontidao: { base: 0, sorte: 0, classe: 0 }, 'auto controle': { base: 0, sorte: 0, classe: 0 }, 'sentir motiv.': { base: 0, sorte: 0, classe: 0 }, sobrevivencia: { base: 0, sorte: 0, classe: 0 }, investigar: { base: 0, sorte: 0, classe: 0 }, blefar: { base: 0, sorte: 0, classe: 0 }, atuacao: { base: 0, sorte: 0, classe: 0 }, diplomacia: { base: 0, sorte: 0, classe: 0 }, 'op. mecanis': { base: 0, sorte: 0, classe: 0 },
    },
    ameaca: { itens: 0, titulos: 0, outros: 0 },
    bonus: {
        bba: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
        bloqueio: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
        reflexo: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
        bbm: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
        percep: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
        racionc: { vantagens: 0, classe: 0, itens: 0, gloria: 0, outros: 0 },
    }
};

// --- Main Component ---
interface CharacterSheetProps {
    sheet: CharacterSheetData;
    setSheet: (data: CharacterSheetData) => void;
}

const CharacterSheet: React.FC<CharacterSheetProps> = ({ sheet, setSheet }) => {
  const [interpretationSuggestion, setInterpretationSuggestion] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  // --- Calculations ---
  const calculateTotal = (attr: { base: number; nivel: number; outros: number }) => attr.base + attr.nivel + attr.outros;
  const totals = {
    forca: calculateTotal(sheet.attributes.forca),
    agilidade: calculateTotal(sheet.attributes.agilidade),
    vigor: calculateTotal(sheet.attributes.vigor),
    sabedoria: calculateTotal(sheet.attributes.sabedoria),
    intuicao: calculateTotal(sheet.attributes.intuicao),
    inteligencia: calculateTotal(sheet.attributes.inteligencia),
    astucia: calculateTotal(sheet.attributes.astucia),
  };

  const impetoCalculado = {
      forca: totals.forca * 3, // kg
      agilidade: Math.floor(totals.agilidade / 3), // mts
      vigor: Math.floor(totals.vigor / 10), // RD
      sabedoria: Math.floor(totals.sabedoria / 10), // RDM
      intuicao: Math.min(Math.floor(totals.intuicao / 20), 3), // Sorte
      inteligencia: Math.floor(totals.inteligencia / 20), // Cdm
      astucia: Math.floor(totals.astucia / 10) // Est
  };

  const bba = Math.floor((totals.forca + totals.agilidade) / 3);
  const bloqueio = Math.floor((totals.forca + totals.vigor) / 3);
  const reflexo = Math.floor((totals.agilidade + totals.astucia) / 3);
  const bbm = Math.floor((totals.sabedoria + totals.inteligencia) / 3);
  const percep = Math.floor((totals.inteligencia + totals.intuicao) / 3);
  const racionc = Math.floor((totals.inteligencia + totals.astucia) / 3);

  const vidaVg = totals.vigor;
  const totalVida = vidaVg + sheet.level + sheet.vida.vt + sheet.renascimentos + sheet.vida.out;

  const essenciaBase = Math.floor((totals.vigor + totals.sabedoria) / 2);
  const totalEssencia = essenciaBase + sheet.level + sheet.essencia.renasc + sheet.essencia.vant + sheet.essencia.outros;
  const essenciaRestante = totalEssencia - sheet.essencia.gastos;

  // FIX: Property 'nivel' does not exist on type 'unknown'. Cast `attr` to `Attribute` to access its properties. This also resolves downstream type errors.
  const spentAttributePoints = Object.values(sheet.attributes).reduce<number>((sum, attr) => sum + (attr as Attribute).nivel, 0);
  const expectedAttributePoints = sheet.level * 3;

  let attributeNotification: string | null = null;
    if (spentAttributePoints > expectedAttributePoints) {
        attributeNotification = `Você distribuiu ${spentAttributePoints - expectedAttributePoints} ponto(s) de nível a mais! (Esperado: ${expectedAttributePoints}, Distribuído: ${spentAttributePoints})`;
    } else if (spentAttributePoints < expectedAttributePoints) {
        attributeNotification = `Você tem ${expectedAttributePoints - spentAttributePoints} ponto(s) de nível para distribuir! (Esperado: ${expectedAttributePoints}, Distribuído: ${spentAttributePoints})`;
    }

  const getLimitador = (level: number): number | string => {
    if (level <= 1) return 10;
    if (level <= 20) return 50;
    if (level <= 25) return 75;
    if (level <= 30) return 100;
    if (level <= 35) return 120;
    return "Renascimento";
  };
  const limitador = getLimitador(sheet.level);

  const totalAmeaca = sheet.level + sheet.ameaca.itens + sheet.ameaca.titulos + sheet.renascimentos + sheet.ameaca.outros;

  // --- Handlers ---
  const handleAttrChange = (attr: keyof Attributes, field: keyof Omit<Attribute, 'impeto'>, value: string) => {
    const numValue = parseInt(value, 10) || 0;
    setSheet({
      ...sheet,
      attributes: {
        ...sheet.attributes,
        [attr]: { ...sheet.attributes[attr], [field]: numValue },
      },
    });
  };

  const handleSheetChange = (section: 'vida' | 'danos' | 'essencia' | 'ameaca', field: string, value: string) => {
      const numValue = parseInt(value, 10) || 0;
      setSheet({
          ...sheet,
          [section]: {
              ...(sheet[section] as any),
              [field]: numValue
          }
      })
  }

  const handleBonusChange = (bonusType: keyof Bonuses, field: keyof BonusDetail, value: string) => {
      const numValue = parseInt(value, 10) || 0;
      setSheet({
          ...sheet,
          bonus: {
              ...sheet.bonus,
              [bonusType]: {
                  ...sheet.bonus[bonusType],
                  [field]: numValue,
              }
          }
      })
  }

  const handleSimpleSheetChange = (field: keyof CharacterSheetData, value: string | number) => {
    setSheet({ ...sheet, [field]: value });
  };

  const handleRenascimentosChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const numValue = parseInt(e.target.value, 10) || 0;
    setSheet({
        ...sheet,
        renascimentos: numValue
    });
  };

  const handleDescricaoChange = (field: keyof CharacterSheetData['descricaoFisica'], value: string | number) => {
    setSheet({
        ...sheet,
        descricaoFisica: { ...sheet.descricaoFisica, [field]: value }
    });
  };

  const handleAptidaoChange = (aptidao: string, field: 'base' | 'sorte' | 'classe', value: string) => {
      const numValue = parseInt(value, 10) || 0;
      setSheet({
          ...sheet,
          aptidoes: {
              ...sheet.aptidoes,
              [aptidao]: {
                  ...sheet.aptidoes[aptidao],
                  [field]: numValue,
              }
          }
      })
  }

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (file.size > 20 * 1024 * 1024) { // 20MB limit
        alert('Arquivo muito grande! Por favor envie imagens com menos de 20MB.');
        return;
      }
      setIsUploading(true);
      const reader = new FileReader();
      reader.onload = (event) => {
        if (event.target && typeof event.target.result === 'string') {
            setSheet({ ...sheet, characterImage: event.target.result as string });
        }
        setIsUploading(false);
      };
      reader.onerror = () => {
          setIsUploading(false);
          alert('Falha ao ler o arquivo.');
      }
      reader.readAsDataURL(file);
    }
  };

  const getSuggestedWeight = (alturaCm: number, genero: string): number => {
    const alturaMetros = alturaCm / 100;
    if (alturaMetros <= 0) return 0;
    const baseBmi = genero === 'Feminino' ? 21 : 22.5;
    return Math.round(baseBmi * (alturaMetros * alturaMetros));
  }

  const handleAlturaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newAltura = parseInt(e.target.value, 10) || 0;
    const newSuggestedWeight = getSuggestedWeight(newAltura, sheet.genero);
    setSheet({
        ...sheet,
        descricaoFisica: {
            ...sheet.descricaoFisica,
            altura: newAltura,
            peso: newSuggestedWeight
        }
    });
  };

  const handleGeneroChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newGenero = e.target.value;
    const newSuggestedWeight = getSuggestedWeight(sheet.descricaoFisica.altura, newGenero);
    setSheet({
        ...sheet,
        genero: newGenero,
        descricaoFisica: {
            ...sheet.descricaoFisica,
            peso: newSuggestedWeight
        }
    });
  };

  const getLevelForXp = (xp: number): number => {
    if (xp < XP_LEVELS[1]) return 0; // Level 0 for XP < 1000
    for (let i = XP_LEVELS.length - 1; i >= 0; i--) {
        if (xp >= XP_LEVELS[i]) {
            return i;
        }
    }
    return 0;
  };

  const handleExperienceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const newXp = parseInt(e.target.value, 10) || 0;
      const newLevel = getLevelForXp(newXp);
      setSheet({
          ...sheet,
          experience: newXp,
          level: newLevel,
      });
  };

  // --- Gemini API Call ---
   const generateInterpretation = useCallback(async () => {
    if (!process.env.API_KEY) {
      alert("API key not found. Please set it up in your environment variables.");
      return;
    }
    setIsLoading(true);
    setInterpretationSuggestion('');
    try {
      const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
      const prompt = `Baseado nos seguintes traços de um personagem de RPG, forneça uma sugestão curta e criativa de como interpretá-lo:
      - Índole: ${sheet.indole}
      - Presença: ${sheet.presenca}
      - Arquétipo de Referência: ${sheet.arquetipo}
      
      Sugestão de Interpretação:`;

      const response = await ai.models.generateContent({
        model: 'gemini-2.5-flash',
        contents: prompt,
      });
      const suggestion = response.text;
      setInterpretationSuggestion(suggestion);
    } catch (error) {
      console.error("Gemini API error:", error);
      setInterpretationSuggestion("Falha ao obter sugestão do Gemini. Verifique o console para mais detalhes.");
    } finally {
      setIsLoading(false);
    }
  }, [sheet.indole, sheet.presenca, sheet.arquetipo]);

    const aptidoesFisicas = Object.entries(sheet.aptidoes).slice(0, 12);
    const aptidoesMentais = Object.entries(sheet.aptidoes).slice(12);

  return (
    <div className="space-y-4">
       {/* Character Image and Description Section */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 p-4 bg-gray-50/50 rounded-lg border border-gray-200 section-container">
            <div className="flex flex-col items-center justify-center print:block">
                {sheet.characterImage ? (
                    <img src={sheet.characterImage} alt="Character" className="w-48 h-48 object-cover rounded-full border-4 border-red-500 shadow-lg"/>
                ) : (
                    <div className="w-48 h-48 bg-gray-200 rounded-full border-4 border-gray-300 flex items-center justify-center text-gray-500">
                        <span>{isUploading ? 'Enviando...' : 'Sem Imagem'}</span>
                    </div>
                )}
                <label className={`mt-4 cursor-pointer bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200 print-hidden ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}>
                    {isUploading ? 'Enviando...' : 'Upload Character Image'}
                    <input type="file" className="hidden" onChange={handleImageUpload} accept="image/*" disabled={isUploading} />
                </label>
                 <div className="w-full mt-4 space-y-2 text-left">
                    <InputField label="Idade" type="number" value={sheet.descricaoFisica.idade} onChange={e => handleDescricaoChange('idade', parseInt(e.target.value) || 0)} />
                    <InputField label="Altura (cm)" type="number" value={sheet.descricaoFisica.altura} onChange={handleAlturaChange} />
                    <InputField label="Peso (kg)" type="number" value={sheet.descricaoFisica.peso} onChange={e => handleDescricaoChange('peso', parseInt(e.target.value) || 0)} />
                    <InputField label="Cor do Cabelo" value={sheet.descricaoFisica.cabeloCor} onChange={e => handleDescricaoChange('cabeloCor', e.target.value)} />
                    <InputField label="Tamanho do Cabelo" value={sheet.descricaoFisica.cabeloTamanho} onChange={e => handleDescricaoChange('cabeloTamanho', e.target.value)} />
                    <InputField label="Cor dos Olhos" value={sheet.descricaoFisica.olhosCor} onChange={e => handleDescricaoChange('olhosCor', e.target.value)} />
                </div>
            </div>
            <div className="md:col-span-2 p-4 bg-white rounded-lg border border-gray-200">
                <h3 className="text-xl font-bold text-red-600 mb-2">Personalidade</h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 mb-4">
                    <SelectField label="Índole" value={sheet.indole} onChange={e => handleSimpleSheetChange('indole', e.target.value)} options={['Bom', 'Mau', 'Neutro']} />
                    <SelectField label="Presença" value={sheet.presenca} onChange={e => handleSimpleSheetChange('presenca', e.target.value)} options={['Bom', 'Leal', 'Caótico', 'Neutro']} />
                    <InputField label="Arquétipo" value={sheet.arquetipo} onChange={e => handleSimpleSheetChange('arquetipo', e.target.value)} />
                </div>
                <button onClick={generateInterpretation} disabled={isLoading} className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200 disabled:bg-gray-500 disabled:cursor-not-allowed print-hidden">
                    {isLoading ? 'Gerando...' : 'Gerar Sugestão de Interpretação'}
                </button>
                <div className="mt-4 p-3 bg-gray-50 rounded-md max-h-48 overflow-y-auto text-sm text-gray-600 border border-gray-200 whitespace-pre-wrap">
                    <p className="font-bold text-red-600">Sugestão de Interpretação:</p>
                    {interpretationSuggestion || 'Clique no botão para receber uma sugestão de interpretação baseada nos seus traços...'}
                </div>
            </div>
        </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 print-single-column">
        {/* Left Column */}
        <div className="space-y-4">
          {/* Informações Pessoais */}
          <div className="section-container">
            <SectionTitle>Informações Pessoais</SectionTitle>
            <div className="p-2 border border-black space-y-2 bg-white">
               <div className="flex gap-2">
                 <InputField label="Jogador" value={sheet.player} onChange={e => handleSimpleSheetChange('player', e.target.value)} />
                 <InputField label="Personagem" value={sheet.character} onChange={e => handleSimpleSheetChange('character', e.target.value)} />
               </div>
               <div className="flex gap-2">
                 <InputField label="Título Heróico" value={sheet.tituloHeroico} onChange={e => handleSimpleSheetChange('tituloHeroico', e.target.value)} />
                 <InputField label="Insólitus" value={sheet.insolitus} onChange={e => handleSimpleSheetChange('insolitus', e.target.value)} />
               </div>
               <div className="flex gap-2">
                <InputField label="Origem" value={sheet.origem} onChange={e => handleSimpleSheetChange('origem', e.target.value)} />
                <SelectField label="Gênero" value={sheet.genero} onChange={handleGeneroChange} options={['Masculino', 'Feminino', 'Outro']} />
               </div>
               <div className="flex gap-2">
                <SelectField label="Classe" value={sheet.classe} onChange={e => handleSimpleSheetChange('classe', e.target.value)} options={CLASSES} />
                 {sheet.classe === 'Outra...' && (
                   <InputField label="Nome da Classe" value={sheet.customClasse} onChange={e => handleSimpleSheetChange('customClasse', e.target.value)} />
                 )}
               </div>
            </div>
          </div>

          {/* Atributos */}
          <div className="section-container">
            <SectionTitle>Atributos</SectionTitle>
             {attributeNotification && (
              <div className="p-2 text-sm text-center bg-yellow-100 text-yellow-800 border-l-4 border-yellow-500 rounded-b-md print-hidden">
                  {attributeNotification}
              </div>
            )}
            <table className="w-full border-collapse border border-black bg-white">
              <thead>
                <tr className="bg-gray-200 text-black text-xs font-bold">
                  <th className="border border-gray-400 p-1">ATRIBUTOS</th>
                  <th className="border border-gray-400 p-1">BASE</th>
                  <th className="border border-gray-400 p-1">NÍVEL</th>
                  <th className="border border-gray-400 p-1">OUTROS</th>
                  <th className="border border-gray-400 p-1">TOTAL</th>
                  <th className="border border-gray-400 p-1">ÍMPETO</th>
                </tr>
              </thead>
              <tbody className="text-black">
                {(Object.entries(sheet.attributes) as [keyof Attributes, Attribute][]).map(([key, value]) => (
                  <tr key={key} className="text-center">
                    <td className="font-bold border border-gray-300 p-1 uppercase">{key}</td>
                    <td className="border border-gray-300 p-0"><input type="number" value={value.base} onChange={e => handleAttrChange(key, 'base', e.target.value)} className="w-full bg-transparent text-center text-black focus:bg-gray-100"/></td>
                    <td className="border border-gray-300 p-0"><input type="number" value={value.nivel} onChange={e => handleAttrChange(key, 'nivel', e.target.value)} className="w-full bg-transparent text-center text-black focus:bg-gray-100"/></td>
                    <td className="border border-gray-300 p-0"><input type="number" value={value.outros} onChange={e => handleAttrChange(key, 'outros', e.target.value)} className="w-full bg-transparent text-center text-black focus:bg-gray-100"/></td>
                    <td className="border border-gray-300 p-1 font-bold">{calculateTotal(value)}</td>
                    <td className="border border-gray-300 p-1 font-bold text-center">{impetoCalculado[key]}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Bônus */}
           <div className="section-container">
              <SectionTitle>Bônus</SectionTitle>
              <div className="p-2 border border-black grid grid-cols-1 md:grid-cols-3 gap-2 bg-white">
                  <BonusField label="B.B.A" baseValue={bba} bonusData={sheet.bonus.bba} onBonusChange={(field, value) => handleBonusChange('bba', field, value)} />
                  <BonusField label="Bloqueio" baseValue={bloqueio} bonusData={sheet.bonus.bloqueio} onBonusChange={(field, value) => handleBonusChange('bloqueio', field, value)} />
                  <BonusField label="Reflexo" baseValue={reflexo} bonusData={sheet.bonus.reflexo} onBonusChange={(field, value) => handleBonusChange('reflexo', field, value)} />
                  <BonusField label="B.B.M" baseValue={bbm} bonusData={sheet.bonus.bbm} onBonusChange={(field, value) => handleBonusChange('bbm', field, value)} />
                  <BonusField label="Percep" baseValue={percep} bonusData={sheet.bonus.percep} onBonusChange={(field, value) => handleBonusChange('percep', field, value)} />
                  <BonusField label="Racionc." baseValue={racionc} bonusData={sheet.bonus.racionc} onBonusChange={(field, value) => handleBonusChange('racionc', field, value)} />
              </div>
          </div>

           {/* Aptidões */}
           <div className="section-container">
            <SectionTitle>Aptidões</SectionTitle>
            <div className="p-2 border border-black bg-white grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-2">
                <div className="space-y-2">
                    <div className="grid grid-cols-5 gap-2 font-bold text-red-600 text-center text-xs">
                        <span className="col-span-2 text-left">FÍSICAS</span>
                        <span>BASE</span>
                        <span>SORTE</span>
                        <span>CLASSE</span>
                        <span>TOTAL</span>
                    </div>
                     {aptidoesFisicas.map(([key, value]) => (
                        <AptidaoInput key={key} label={key} baseValue={(value as Aptidao).base} sorteValue={(value as Aptidao).sorte} classeValue={(value as Aptidao).classe} onBaseChange={e => handleAptidaoChange(key, 'base', e.target.value)} onSorteChange={e => handleAptidaoChange(key, 'sorte', e.target.value)} onClasseChange={e => handleAptidaoChange(key, 'classe', e.target.value)} />
                    ))}
                </div>
                <div className="space-y-2">
                    <div className="grid grid-cols-5 gap-2 font-bold text-red-600 text-center text-xs">
                        <span className="col-span-2 text-left">MENTAIS</span>
                        <span>BASE</span>
                        <span>SORTE</span>
                        <span>CLASSE</span>
                        <span>TOTAL</span>
                    </div>
                    {aptidoesMentais.map(([key, value]) => (
                        <AptidaoInput key={key} label={key} baseValue={(value as Aptidao).base} sorteValue={(value as Aptidao).sorte} classeValue={(value as Aptidao).classe} onBaseChange={e => handleAptidaoChange(key, 'base', e.target.value)} onSorteChange={e => handleAptidaoChange(key, 'sorte', e.target.value)} onClasseChange={e => handleAptidaoChange(key, 'classe', e.target.value)} />
                    ))}
                </div>
            </div>
           </div>

        </div>

        {/* Right Column */}
        <div className="space-y-4 print-break-before-page">
          {/* Desenvolvimento */}
          <div className="section-container">
            <SectionTitle>Desenvolvimento</SectionTitle>
             <div className="p-2 border border-black grid grid-cols-2 sm:grid-cols-4 gap-2 bg-white">
                <ValueBox label="Nível" value={sheet.level}/>
                <InputField label="Experiência" value={sheet.experience} onChange={handleExperienceChange} type="number" />
                <InputField label="Renascimentos" value={sheet.renascimentos} onChange={handleRenascimentosChange} type="number" />
                <ValueBox label="limitador" value={limitador} small={true}/>
             </div>
          </div>

          {/* Ameaça */}
           <div className="section-container">
            <RedSectionTitle>Ameaça</RedSectionTitle>
             <div className="p-2 border border-black grid grid-cols-2 sm:grid-cols-5 gap-2 bg-white">
                <ValueBox label="Nível" value={sheet.level}/>
                <InputField label="Itens" value={sheet.ameaca.itens} onChange={e => handleSheetChange('ameaca', 'itens', e.target.value)} type="number"/>
                <InputField label="Títulos" value={sheet.ameaca.titulos} onChange={e => handleSheetChange('ameaca', 'titulos', e.target.value)} type="number"/>
                <ValueBox label="Renasc." value={sheet.renascimentos}/>
                <InputField label="Outros" value={sheet.ameaca.outros} onChange={e => handleSheetChange('ameaca', 'outros', e.target.value)} type="number"/>
                <div className="col-span-full">
                    <ValueBox label="Total" value={totalAmeaca}/>
                </div>
             </div>
          </div>

          {/* Vida */}
          <div className="section-container">
            <SectionTitle>Vida</SectionTitle>
            <div className="p-2 border border-black space-y-2 bg-white">
                <div className="grid grid-cols-2 sm:grid-cols-5 gap-2 mb-2">
                    <ValueBox label="VG" value={vidaVg}/>
                    <ValueBox label="NV" value={sheet.level}/>
                    <InputField label="VT" value={sheet.vida.vt} onChange={e => handleSheetChange('vida', 'vt', e.target.value)} type="number"/>
                    <ValueBox label="RN" value={sheet.renascimentos}/>
                    <InputField label="OUT" value={sheet.vida.out} onChange={e => handleSheetChange('vida', 'out', e.target.value)} type="number"/>
                </div>
                <div className="text-center mb-2">
                    <span className="text-xs text-gray-500">TOTAL DE VIDA: </span>
                    <span className="font-bold text-lg text-black">{totalVida}</span>
                </div>
                <table className="w-full text-center border-collapse">
                    <thead>
                        <tr className="bg-gray-200 text-black text-xs font-bold">
                            <th className="border border-gray-400 p-1">Membro</th>
                            <th className="border border-gray-400 p-1">%</th>
                            <th className="border border-gray-400 p-1">Valor</th>
                            <th className="border border-gray-400 p-1">Danos</th>
                        </tr>
                    </thead>
                    <tbody className="text-black">
                        {[
                            { name: 'Cabeça', perc: 75, key: 'cabeça' },
                            { name: 'Tronco', perc: 100, key: 'tronco' },
                            { name: 'Braço.D', perc: 25, key: 'bracoD' },
                            { name: 'Braço.E', perc: 25, key: 'bracoE' },
                            { name: 'Perna.D', perc: 25, key: 'pernaD' },
                            { name: 'Perna.E', perc: 25, key: 'pernaE' },
                            { name: 'Sangue', perc: 100, key: 'sangue' },
                        ].map(m => {
                            const valorBase = Math.floor(totalVida * (m.perc / 100));
                            const dano = sheet.danos[m.key as keyof typeof sheet.danos];
                            return (
                             <tr key={m.name}>
                                <td className="border border-gray-300 p-1 font-semibold">{m.name}</td>
                                <td className="border border-gray-300 p-1 text-sm">{m.perc}%</td>
                                <td className="border border-gray-300 p-1 font-bold">{valorBase - dano}</td>
                                <td className="border border-gray-300 p-0"><input type="number" value={dano} onChange={e => handleSheetChange('danos', m.key, e.target.value)} className="w-full bg-transparent text-center text-black focus:bg-gray-100"/></td>
                            </tr>
                        )})}
                    </tbody>
                </table>
            </div>
          </div>

            {/* Essência */}
           <div className="section-container">
            <RedSectionTitle>Essência</RedSectionTitle>
             <div className="p-2 border border-black space-y-2 bg-white">
                <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
                    <ValueBox label="V+S/2" value={essenciaBase}/>
                    <ValueBox label="Nível" value={sheet.level}/>
                    <InputField label="Renasc" value={sheet.essencia.renasc} onChange={e => handleSheetChange('essencia', 'renasc', e.target.value)} type="number"/>
                    <InputField label="Vant" value={sheet.essencia.vant} onChange={e => handleSheetChange('essencia', 'vant', e.target.value)} type="number"/>
                    <InputField label="Outros" value={sheet.essencia.outros} onChange={e => handleSheetChange('essencia', 'outros', e.target.value)} type="number"/>
                </div>
                 <div className="text-center my-2">
                    <span className="text-xs text-gray-500">ESSÊNCIA RESTANTE: </span>
                    <span className="font-bold text-lg">{essenciaRestante} / {totalEssencia}</span>
                </div>
                <InputField label="Gastos" value={sheet.essencia.gastos} onChange={e => handleSheetChange('essencia', 'gastos', e.target.value)} type="number"/>
             </div>
          </div>

          <div className="print-hidden">
            <Prospeccao />
          </div>

        </div>
      </div>
    </div>
  );
};

export default CharacterSheet;
