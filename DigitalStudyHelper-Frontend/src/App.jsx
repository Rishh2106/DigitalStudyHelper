import { useState, useEffect } from 'react'
import './App.css'
import { createStudyResource, getStudyResources } from './services/studyResourceService'

function App() {
  const [url, setUrl] = useState('')
  const [name, setName] = useState('')
  const [generatedResource, setGeneratedResource] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const fetchResources = async () => {
      try {
        const resources = await getStudyResources()
        if (resources.length > 0) {
          setGeneratedResource(resources[resources.length - 1])
        }
      } catch (error) {
        setError('Failed to fetch study resources')
      }
    }
    fetchResources()
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const resourceData = { url, name }
      const createdResource = await createStudyResource(resourceData)
      setGeneratedResource(createdResource)
      setUrl('')
      setName('')
    } catch (error) {
      setError('Failed to create study resource. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app-container">
      <h1>Digital Study Helper</h1>
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="url">Study Resource URL:</label>
          <input
            type="url"
            id="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="Enter study resource URL (e.g., https://example.com)"
            className={error && !url ? 'error-input' : ''}
            disabled={loading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="name">Resource Name:</label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter resource name"
            className={error && !name ? 'error-input' : ''}
            disabled={loading}
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create Study Resource'}
        </button>
      </form>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {generatedResource && (
        <div className="hyperlink-container">
          <h4>Study Resource:</h4>
          <a
            href={generatedResource.url}
            target="_blank"
            rel="noopener noreferrer"
          >
            {generatedResource.name}
          </a>
        </div>
      )}
    </div>
  )
}

export default App
