import { useState, useEffect } from 'react';
import './GroupList.css';

function GroupList({ onGroupSelect }) {
    const [groups, setGroups] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetchGroups();
    }, []);

    const fetchGroups = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch('http://localhost:8080/api/groups', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch groups');
            }

            const data = await response.json();
            setGroups(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDelete = async (groupId) => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                throw new Error('No authentication token found');
            }

            const response = await fetch(`http://localhost:8080/api/groups/${groupId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to delete group');
            }

            setGroups(groups.filter(group => group.id !== groupId));
        } catch (err) {
            setError(err.message);
        }
    };

    if (isLoading) {
        return <div className="loading">Loading groups...</div>;
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    return (
        <div className="group-list">
            <h2>Your Groups</h2>
            {groups.length === 0 ? (
                <p>No groups created yet. Create your first group!</p>
            ) : (
                <div className="groups-grid">
                    {groups.map(group => (
                        <div key={group.id} className="group-card">
                            <h3>{group.name}</h3>
                            <p>Links: {group.linkCount}</p>
                            <p>Created: {new Date(group.createdAt).toLocaleDateString()}</p>
                            <div className="group-actions">
                                <button 
                                    onClick={() => onGroupSelect(group.id)}
                                    className="view-btn"
                                >
                                    View Links
                                </button>
                                <button 
                                    onClick={() => handleDelete(group.id)}
                                    className="delete-btn"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default GroupList; 