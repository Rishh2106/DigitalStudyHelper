import { useState, useEffect } from 'react'
import './App.css'
import AuthForm from './components/AuthForm'

function App() {
  const [url, setUrl] = useState('')
  const [name, setName] = useState('')
  const [links, setLinks] = useState([])
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [currentUser, setCurrentUser] = useState(null)
  const [error, setError] = useState(null)
  const [isRegistering, setIsRegistering] = useState(false)

  useEffect(() => {
    if (isAuthenticated) {
      fetchLinks()
    }
  }, [isAuthenticated])

  const fetchLinks = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        setError('Please login to view links')
        return
      }

      console.log('Fetching links with token:', token)
      
      const response = await fetch('http://localhost:8080/api/links', {
        headers: {
          'Authorization': `Basic ${token}`
        }
      })

      console.log('Fetch links response status:', response.status)
      
      if (!response.ok) {
        const errorText = await response.text()
        console.error('Fetch links error response:', errorText)
        let errorMessage = 'Failed to fetch links'
        try {
          const errorData = JSON.parse(errorText)
          errorMessage = errorData.error || errorMessage
        } catch (parseError) {
          console.error('Failed to parse error response:', parseError)
        }
        throw new Error(errorMessage)
      }

      const data = await response.json()
      console.log('Fetch links success response:', data)
      
      setLinks(data)
      setError(null)
    } catch (err) {
      console.error('Error fetching links:', err)
      setError(err.message)
    }
  }

  const handleAuthSuccess = (user) => {
    setIsAuthenticated(true)
    setCurrentUser(user)
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    setIsAuthenticated(false)
    setCurrentUser(null)
    setLinks([])
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!url || !name) {
      setError('Please fill in all fields')
      return
    }

    try {
      const token = localStorage.getItem('token')
      if (!token) {
        setError('Please login to create links')
        return
      }

      console.log('Creating link with:', { url, name })
      console.log('Using token:', token)
      
      const response = await fetch('http://localhost:8080/api/create-link', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${token}`
        },
        body: JSON.stringify({ url, name })
      })

      console.log('Create link response status:', response.status)
      
      if (!response.ok) {
        const errorText = await response.text()
        console.error('Create link error response:', errorText)
        let errorMessage = 'Failed to create link'
        try {
          const errorData = JSON.parse(errorText)
          errorMessage = errorData.error || errorMessage
        } catch (parseError) {
          console.error('Failed to parse error response:', parseError)
        }
        throw new Error(errorMessage)
      }

      const data = await response.json()
      console.log('Create link success response:', data)
      
      setLinks([data, ...links])
      setError(null)
      setUrl('')
      setName('')
    } catch (err) {
      console.error('Error creating link:', err)
      setError(err.message)
    }
  }

  const handleDelete = async (id) => {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        setError('Please login to delete links')
        return
      }

      const response = await fetch(`http://localhost:8080/api/links/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Basic ${token}`
        }
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.error || 'Failed to delete link')
      }

      setLinks(links.filter(link => link.id !== id))
    } catch (err) {
      setError(err.message)
      console.error('Error:', err)
    }
  }

  if (!isAuthenticated) {
    return <AuthForm onAuthSuccess={handleAuthSuccess} />
  }

  return (
    <div className="app">
      <header>
        <h1>Digital Study Helper</h1>
        <div className="user-info">
          <span>Welcome, {currentUser.username}</span>
          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </header>
      <main>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="url">URL:</label>
            <input
              type="text"
              id="url"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="Enter URL (e.g., https://example.com)"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="name">Link Name:</label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter link name"
              required
            />
          </div>
          <button type="submit">Create Link</button>
        </form>

        {error && <div className="error">{error}</div>}

        <div className="links-list">
          <h2>Your Links</h2>
          {links.length === 0 ? (
            <p>No entries</p>
          ) : (
            links.map(link => (
              <div key={link.id} className="link-item">
                <a href={link.url} target="_blank" rel="noopener noreferrer">
                  {link.name}
                </a>
                <button onClick={() => handleDelete(link.id)}>Delete</button>
              </div>
            ))
          )}
        </div>
      </main>
    </div>
  )
}

export default App

