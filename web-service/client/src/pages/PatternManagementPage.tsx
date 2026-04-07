import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getErrorMessage } from '../services/http'
import { patternApi } from '../services/patternApi'
import type { VioPatternResponse } from '../types/pattern'

export function PatternManagementPage() {
  const navigate = useNavigate()

  const [keyword, setKeyword] = useState('')
  const [results, setResults] = useState<VioPatternResponse[]>([])
  const [hasSearched, setHasSearched] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  const executeSearch = async () => {
    setIsLoading(true)
    setError('')

    try {
      const allPatterns = await patternApi.list()
      const normalizedKeyword = keyword.trim().toLowerCase()

      const filtered = normalizedKeyword
        ? allPatterns.filter((pattern) =>
            pattern.name.toLowerCase().includes(normalizedKeyword),
          )
        : allPatterns

      setResults(filtered)
      setHasSearched(true)
    } catch (err) {
      setError(getErrorMessage(err))
      setHasSearched(true)
      setResults([])
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm('Delete this violence pattern?')
    if (!confirmed) {
      return
    }

    setError('')

    try {
      await patternApi.remove(id)
      await executeSearch()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  const handleBack = () => {
    navigate('/manager-home')
  }

  return (
    <section className="simple-page">
      <article className="panel">
        <div className="panel-header-row">
          <div>
            <h2>Violence Patterns</h2>
            <p className="panel-subtitle">
              Search and manage violence pattern records.
            </p>
          </div>
          <span className="role-badge role-badge-inline">Admin</span>
        </div>

        {error && <p className="banner error">{error}</p>}

        <div className="search-row">
          <input
            type="text"
            value={keyword}
            placeholder="Enter violence pattern name"
            onChange={(event) => setKeyword(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                void executeSearch()
              }
            }}
          />
          <button
            type="button"
            className="primary-button"
            onClick={() => void executeSearch()}
            disabled={isLoading}
          >
            {isLoading ? 'Searching...' : 'Search'}
          </button>
        </div>

        {!hasSearched ? (
          <p className="muted-text table-hint">
            Click Search to load violence patterns.
          </p>
        ) : (
          <div className="table-wrap pattern-table-shell">
            <table>
              <thead>
                <tr>
                  <th rowSpan={2}>No.</th>
                  <th rowSpan={2}>Name</th>
                  <th rowSpan={2}>Severity Level</th>
                  <th rowSpan={2}>Select</th>
                  <th colSpan={2}>Action</th>
                </tr>
                <tr>
                  <th>Modify</th>
                  <th>Delete</th>
                </tr>
              </thead>
              <tbody>
                {results.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="empty-cell">
                      No violence pattern found.
                    </td>
                  </tr>
                ) : (
                  results.map((pattern, index) => (
                    <tr key={pattern.id}>
                      <td>{index + 1}</td>
                      <td>{pattern.name}</td>
                      <td>{pattern.sevLevel}</td>
                      <td>
                        <button
                          type="button"
                          className="link-button"
                          onClick={() => navigate(`/patterns/${pattern.id}`)}
                        >
                          Detail
                        </button>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="link-button"
                          onClick={() => navigate(`/patterns/${pattern.id}/edit`)}
                        >
                          Modify
                        </button>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="link-button danger"
                          onClick={() => void handleDelete(pattern.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        <div className="panel-actions panel-actions-spread">
          <button type="button" className="secondary-button" onClick={handleBack}>
            Back
          </button>
          <button
            type="button"
            className="primary-button"
            onClick={() => navigate('/patterns/add')}
          >
            Add
          </button>
        </div>
      </article>
    </section>
  )
}
