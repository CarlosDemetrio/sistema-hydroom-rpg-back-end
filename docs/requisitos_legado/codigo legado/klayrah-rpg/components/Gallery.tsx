import React, { useState, useEffect, useCallback } from 'react';
import { GoogleGenAI } from '@google/genai';

interface GalleryProps {
  galleryId: string;
  title: string;
}

interface ImageItem {
  id: string;
  src: string;
  name: string;
  analysis?: string;
}

const Gallery: React.FC<GalleryProps> = ({ galleryId, title }) => {
  const [images, setImages] = useState<ImageItem[]>([]);
  const [selectedImage, setSelectedImage] = useState<ImageItem | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);


  useEffect(() => {
    const savedImages = localStorage.getItem(galleryId);
    if (savedImages) {
      setImages(JSON.parse(savedImages));
    } else {
      setImages([]); // Clear images if switching to a sheet with no gallery
    }
  }, [galleryId]);

  const saveImages = (newImages: ImageItem[]) => {
    setImages(newImages);
    localStorage.setItem(galleryId, JSON.stringify(newImages));
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
        const files: File[] = Array.from(e.target.files);

        // FIX: Property 'size' does not exist on type 'unknown'. Explicitly type `file` as `File` to access its properties. This also fixes the error on `f.name` below.
        const largeFiles = files.filter((file: File) => file.size > 20 * 1024 * 1024); // 20MB limit
        if (largeFiles.length > 0) {
            alert(`Os seguintes arquivos são muito grandes (máx 20MB): ${largeFiles.map(f => f.name).join(', ')}`);
            return;
        }

        setIsUploading(true);
        let processedCount = 0;
        let newImagesBatch: ImageItem[] = [];

        if (files.length === 0) {
            setIsUploading(false);
            return;
        }

        files.forEach((file: File) => {
            const reader = new FileReader();
            reader.onload = (event) => {
                processedCount++;
                if (event.target?.result) {
                    newImagesBatch.push({
                        id: `${Date.now()}-${file.name}`,
                        src: event.target.result as string,
                        name: file.name,
                    });
                }

                if (processedCount === files.length) {
                    setImages(prevImages => {
                        const allNewImages = [...prevImages, ...newImagesBatch];
                        localStorage.setItem(galleryId, JSON.stringify(allNewImages));
                        return allNewImages;
                    });
                    setIsUploading(false);
                }
            };
            reader.onerror = () => {
                processedCount++;
                 if (processedCount === files.length) {
                    setIsUploading(false);
                }
                alert(`Falha ao ler o arquivo: ${file.name}`);
            }
            reader.readAsDataURL(file);
        });
    }
  };

  const handleDeleteImage = (id: string) => {
    if (window.confirm('Are you sure you want to delete this image?')) {
      const filteredImages = images.filter(img => img.id !== id);
      saveImages(filteredImages);
      if(selectedImage?.id === id) {
          setSelectedImage(null);
      }
    }
  };

  const handleAnalyzeImage = useCallback(async (image: ImageItem) => {
    if (!process.env.API_KEY) {
        alert("API key not found.");
        return;
    }
    if (!image) return;
    setIsLoading(true);
    setSelectedImage({...image, analysis: 'Analyzing...'});

    try {
        const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
        const base64Data = image.src.split(',')[1];
        const imagePart = {
            inlineData: {
                data: base64Data,
                mimeType: image.src.match(/:(.*?);/)?.[1] || 'image/jpeg',
            },
        };

        const prompt = galleryId.startsWith('items')
            ? "Describe this item from a fantasy RPG. What could it be? What are its potential powers or history? Be creative."
            : "Describe this character or scene. What is happening? What is the mood? What story does this image tell?";
        
        const textPart = { text: prompt };

        const response = await ai.models.generateContent({
            model: 'gemini-2.5-flash',
            contents: { parts: [imagePart, textPart] },
        });

        const newAnalysis = response.text;
        const updatedImages = images.map(img => 
            img.id === image.id ? { ...img, analysis: newAnalysis } : img
        );
        saveImages(updatedImages);
        setSelectedImage({ ...image, analysis: newAnalysis });

    } catch (error) {
        console.error("Gemini image analysis error:", error);
        const errorAnalysis = "Failed to analyze image. Please check the console for details.";
        const updatedImages = images.map(img => 
            img.id === image.id ? { ...img, analysis: errorAnalysis } : img
        );
        saveImages(updatedImages);
        setSelectedImage({ ...image, analysis: errorAnalysis });
    } finally {
        setIsLoading(false);
    }
  }, [galleryId, images]);

  return (
    <div>
      <h2 className="text-2xl font-bold text-red-600 mb-4">{title}</h2>
      <div className="mb-4">
        <label className={`cursor-pointer bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200 ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}>
          {isUploading ? 'Enviando...' : 'Upload Image(s)'}
          <input type="file" multiple className="hidden" onChange={handleImageUpload} accept="image/*" disabled={isUploading}/>
        </label>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="md:col-span-1 max-h-96 md:max-h-full overflow-y-auto pr-2 border-r border-gray-200">
            {images.length === 0 ? (
                 <p className="text-gray-500">No images uploaded yet.</p>
            ): (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-2 gap-2">
                    {images.map(img => (
                        <div key={img.id} onClick={() => setSelectedImage(img)} className={`cursor-pointer border-2 rounded-lg overflow-hidden ${selectedImage?.id === img.id ? 'border-red-500' : 'border-transparent hover:border-red-400'}`}>
                            <img src={img.src} alt={img.name} className="w-full h-24 object-cover" />
                        </div>
                    ))}
                </div>
            )}
        </div>
        <div className="md:col-span-2 p-4 bg-gray-50 rounded-lg min-h-[300px]">
            {selectedImage ? (
                <div className="flex flex-col md:flex-row gap-4">
                    <div className="flex-shrink-0">
                        <img src={selectedImage.src} alt={selectedImage.name} className="w-full md:w-48 h-auto max-h-64 object-contain rounded-lg shadow-lg"/>
                        <button onClick={() => handleDeleteImage(selectedImage.id)} className="w-full mt-2 bg-gray-200 hover:bg-gray-300 text-red-600 text-sm font-bold py-1 px-2 rounded transition-colors duration-200">
                          Delete
                        </button>
                    </div>
                    <div>
                        <h3 className="text-lg font-bold truncate mb-2">{selectedImage.name}</h3>
                         <button onClick={() => handleAnalyzeImage(selectedImage)} disabled={isLoading} className="mb-4 bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition-colors duration-200 disabled:bg-gray-500">
                            {isLoading ? 'Analyzing...' : 'Analyze with Gemini'}
                        </button>
                        <div className="text-sm text-gray-600 bg-gray-100 p-3 rounded-md max-h-64 overflow-y-auto whitespace-pre-wrap border border-gray-200">
                            <p className="font-bold text-red-600">Gemini Analysis:</p>
                            <p>{selectedImage.analysis || 'Click the button to get an analysis.'}</p>
                        </div>
                    </div>
                </div>
            ) : (
                <div className="flex items-center justify-center h-full text-gray-500">
                    <p>Select an image to view details.</p>
                </div>
            )}
        </div>
      </div>
    </div>
  );
};

export default Gallery;