import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { getErrorMessage } from '../services/http'
import { recognitionApi } from '../services/recognitionApi'
import type { RecognitionModel, RecognitionWorkflowState } from '../types/recognition'

interface RecognitionRouteState {
  workflow?: RecognitionWorkflowState
}

export function RecognitionModelSelectionPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth()

  const routeState = location.state as RecognitionRouteState | null
  const workflow = routeState?.workflow

  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [models, setModels] = useState<RecognitionModel[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [choosingModelId, setChoosingModelId] = useState<number | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadModels = async () => {
      setIsLoading(true)
      setError('')
      try {
        const result = await recognitionApi.listModels(searchKeyword || undefined)
        setModels(result)
      } catch (err) {
        setError(getErrorMessage(err))
      } finally {
        setIsLoading(false)
      }
    }

    void loadModels()
  }, [searchKeyword])

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSearchKeyword(keyword.trim())
  }

  const handleChooseModel = async (model: RecognitionModel) => {
    if (!workflow?.uploadedSource) {
      setError('Video has not been uploaded yet. Please go back and upload first.')
      return
    }

    setChoosingModelId(model.id)
    setError('')

    try {
      const recognitionResult = await recognitionApi.executeRecognition({
        source: workflow.uploadedSource,
        sourceId: workflow.uploadId,
        modelId: model.id,
        userId: user?.id,
      })

      const nextWorkflow: RecognitionWorkflowState = {
        ...workflow,
        selectedModel: model,
        recognitionResult,
      }

      navigate('/recognition/result', { state: { workflow: nextWorkflow } })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setChoosingModelId(null)
    }
  }

  if (!workflow) {
    return (
      <section className="simple-page">
        <article className="panel">
          <h2>Model Selection</h2>
          <p className="banner error">
            Missing video context. Please go back to select and upload a video first.
          </p>
          <div className="panel-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={() => navigate('/recognition/video')}
            >
              Back
            </button>
          </div>
        </article>
      </section>
    )
  }

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>Model Selection</h2>
        <p className="panel-subtitle">
          Search model by name, then choose one model to run violence recognition.
        </p>

        <p className="muted-text">Uploaded video: {workflow.selectedFileName}</p>
        {error && <p className="banner error">{error}</p>}

        <form className="search-row" onSubmit={handleSearch}>
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Enter model name"
          />
          <button type="submit" className="primary-button" disabled={isLoading}>
            Search
          </button>
        </form>

        <div className="pattern-table-shell">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>No.</th>
                  <th>Name</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td className="empty-cell" colSpan={3}>
                      Loading models...
                    </td>
                  </tr>
                ) : models.length === 0 ? (
                  <tr>
                    <td className="empty-cell" colSpan={3}>
                      No model found.
                    </td>
                  </tr>
                ) : (
                  models.map((model, index) => (
                    <tr key={model.id}>
                      <td>{index + 1}</td>
                      <td>{model.name}</td>
                      <td>
                        <button
                          type="button"
                          className="link-button"
                          onClick={() => void handleChooseModel(model)}
                          disabled={choosingModelId === model.id}
                        >
                          {choosingModelId === model.id ? 'Choosing...' : 'Choose'}
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="panel-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => navigate('/recognition/video', { state: { workflow } })}
          >
            Back
          </button>
        </div>
      </article>
    </section>
  )
}
