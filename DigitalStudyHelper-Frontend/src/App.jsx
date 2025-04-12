import { useState, useEffect } from 'react'
import './App.css'
import AuthForm from './components/AuthForm'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [currentUser, setCurrentUser] = useState(null)
  const [links, setLinks] = useState([])
  const [url, setUrl] = useState('')
  const [hyperlink, setHyperlink] = useState('')
  const [error, setError] = useState(null)

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setIsAuthenticated(true);
      // You might want to validate the token here
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      fetchLinks();
    }
  }, [isAuthenticated]);

  const fetchLinks = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token found');
      }

      const response = await fetch('http://localhost:8080/api/links', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.status === 401) {
        // Token expired or invalid
        handleLogout();
        throw new Error('Session expired. Please login again.');
      }

      if (!response.ok) {
        throw new Error('Failed to fetch links');
      }

      const data = await response.json();
      setLinks(data);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAuthSuccess = (userData) => {
    setIsAuthenticated(true);
    setCurrentUser(userData);
    setError(null);
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    setCurrentUser(null);
    setLinks([]);
    localStorage.removeItem('token');
    setError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token found');
      }

      const response = await fetch('http://localhost:8080/api/links', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ url, hyperlink })
      });

      if (response.status === 401) {
        handleLogout();
        throw new Error('Session expired. Please login again.');
      }

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to create link');
      }

      const newLink = await response.json();
      setLinks([...links, newLink]);
      setUrl('');
      setHyperlink('');
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (id) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token found');
      }

      const response = await fetch(`http://localhost:8080/api/links/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.status === 401) {
        handleLogout();
        throw new Error('Session expired. Please login again.');
      }

      if (!response.ok) {
        throw new Error('Failed to delete link');
      }

      setLinks(links.filter(link => link.id !== id));
    } catch (err) {
      setError(err.message);
    }
  };

  if (!isAuthenticated) {
    return <AuthForm onAuthSuccess={handleAuthSuccess} />;
  }

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>Digital Study Helper</h1>
        <div className="user-info">
          <span>Welcome, {currentUser?.username}!</span>
          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </header>

      <main className="main-content">
        <div className="link-form">
          <h2>Create New Link</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="url">URL:</label>
              <input
                type="url"
                id="url"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                required
                placeholder="Enter URL"
                maxLength={2048}
                pattern="https?://.+"
                title="Please enter a valid URL starting with http:// or https://"
              />
            </div>
            <div className="form-group">
              <label htmlFor="hyperlink">Hyperlink Text:</label>
              <input
                type="text"
                id="hyperlink"
                value={hyperlink}
                onChange={(e) => setHyperlink(e.target.value)}
                required
                placeholder="Enter hyperlink text"
              />
            </div>
            <button type="submit" className="submit-btn">Create Link</button>
          </form>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="links-list">
          <h2>Your Links</h2>
          {links.length === 0 ? (
            <p>No links created yet. Create your first link above!</p>
          ) : (
            <ul>
              {links.map(link => (
                <li key={link.id} className="link-item">
                  <a href={link.url} target="_blank" rel="noopener noreferrer">
                    {link.hyperlink}
                  </a>
                  <button
                    onClick={() => handleDelete(link.id)}
                    className="delete-btn"
                  >
                    Delete
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </main>
    </div>
  );
}

export default App;

