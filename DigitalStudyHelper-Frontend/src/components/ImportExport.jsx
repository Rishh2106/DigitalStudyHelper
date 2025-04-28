import { useState } from 'react';
import './ImportExport.css';

function ImportExport({ onImportSuccess }) {
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleExport = async () => {
        try {
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

        try {
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No authentication token found');
            }

            const reader = new FileReader();
            reader.onload = async (e) => {
                try {
                    const data = JSON.parse(e.target.result);
                    
                    const response = await fetch('http://localhost:8080/api/groups/import', {
                        method: 'POST',
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(data)
                    });

                    if (!response.ok) {
                        const errorData = await response.json();
                        throw new Error(errorData.error || 'Failed to import groups');
                    }

                    const result = await response.json();
                    onImportSuccess(result);
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