import { useState, useEffect } from 'react';
import './ImportExport.css';

function ImportExport({ onImportSuccess }) {
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        // Check for success state on component mount
        const importSuccess = localStorage.getItem('importSuccess');
        if (importSuccess === 'true') {
            setSuccess(true);
            const timer = setTimeout(() => {
                setSuccess(false);
                localStorage.removeItem('importSuccess');
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, []);

    const getCollectionsCount = async (token) => {
        const response = await fetch('http://localhost:8080/api/groups', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) {
            throw new Error('Failed to fetch collections');
        }
        const collections = await response.json();
        return collections.length;
    };

    const handleExport = async () => {
        try {
            setError(null);
            setSuccess(false);
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch('http://localhost:8080/api/groups/export', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to export groups');
            }

            const data = await response.json();
            
            // Create a download link
            const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'study_groups_export.json';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleImport = async (event) => {
        const file = event.target.files[0];
        if (!file) return;

        setIsLoading(true);
        setError(null);
        setSuccess(false);

        try {
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No authentication token found');
            }

            const initialCount = await getCollectionsCount(token);

            const reader = new FileReader();
            reader.onload = async (e) => {
                try {
                    let data;
                    try {
                        data = JSON.parse(e.target.result);
                    } catch (parseError) {
                        const fixedJson = e.target.result
                            .replace(/([{,]\s*)(\w+)(\s*:)/g, '$1"$2"$3')
                            .replace(/,(\s*[}\]])/g, '$1');
                        
                        try {
                            data = JSON.parse(fixedJson);
                        } catch (fixedError) {
                            const errorMessage = parseError.message;
                            const position = errorMessage.match(/position (\d+)/);
                            if (position) {
                                const pos = parseInt(position[1]);
                                const context = e.target.result.substring(Math.max(0, pos - 50), Math.min(e.target.result.length, pos + 50));
                                throw new Error(`Invalid JSON format at position ${pos}:\n${context}\n${errorMessage}`);
                            }
                            throw new Error(`Invalid JSON format: ${errorMessage}`);
                        }
                    }
                    
                    const importData = Array.isArray(data) ? data : [data];
                    
                    importData.forEach((collection, index) => {
                        if (!collection || typeof collection !== 'object') {
                            throw new Error(`Invalid collection at index ${index}`);
                        }
                        if (!collection.name) {
                            throw new Error(`Collection at index ${index} is missing a name`);
                        }
                    });
                    
                    const response = await fetch('http://localhost:8080/api/groups/import', {
                        method: 'POST',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(importData)
                    });

                    if (!response.ok) {
                        const errorData = await response.json();
                        throw new Error(errorData.error || 'Failed to import groups');
                    }

                    const finalCount = await getCollectionsCount(token);
                    
                    // Set success state in localStorage before refreshing
                    localStorage.setItem('importSuccess', 'true');
                    
                    // Refresh the UI
                    if (onImportSuccess) {
                        await onImportSuccess();
                    }
                } catch (err) {
                    setError(err.message);
                } finally {
                    setIsLoading(false);
                }
            };
            reader.readAsText(file);
        } catch (err) {
            setError(err.message);
            setIsLoading(false);
        }
    };

    return (
        <div className="import-export">
            <h2>Import/Export Collections</h2>
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">Collections imported successfully!</div>}
            <div className="buttons">
                <button 
                    onClick={handleExport}
                    disabled={isLoading}
                    className="export-btn"
                >
                    Export Collections
                </button>
                <label className="import-btn">
                    Import Collections
                    <input
                        type="file"
                        accept=".json"
                        onChange={handleImport}
                        disabled={isLoading}
                        style={{ display: 'none' }}
                    />
                </label>
            </div>
            {isLoading && <div className="loading">Processing...</div>}
        </div>
    );
}

export default ImportExport; 