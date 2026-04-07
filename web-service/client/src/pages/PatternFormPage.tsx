import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import type { ChangeEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getErrorMessage } from '../services/http'
import { patternApi } from '../services/patternApi'
import type { VioPatternRequest } from '../types/pattern'

const EMPTY_FORM: VioPatternRequest = {
  name: '',
  sevLevel: 0,
  threshold: 0.5,
  file: '',
}

export function PatternFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [form, setForm] = useState<VioPatternRequest>(EMPTY_FORM)
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [error, setError] = useState('')

  const isEditMode = useMemo(() => typeof id === 'string', [id])

  useEffect(() => {
    if (!isEditMode) {
      return
    }

    const patternId = Number(id)
    if (Number.isNaN(patternId)) {
      setError('Invalid pattern id.')
      return
    }

    const loadPattern = async () => {
      setIsLoading(true)
      try {
        const result = await patternApi.findById(patternId)
        setForm({
          name: result.name,
          sevLevel: result.sevLevel,
          threshold: result.threshold,
          file: result.file || '',
        })
      } catch (err) {
        setError(getErrorMessage(err))
      } finally {
        setIsLoading(false)
      }
    }

    void loadPattern()
  }, [id, isEditMode])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')

    if (form.threshold < 0 || form.threshold > 1) {
      setError('Threshold must be between 0 and 1.')
      return
    }

    setIsSaving(true)

    try {
      if (isEditMode) {
        await patternApi.update(Number(id), form)
      } else {
        await patternApi.create(form)
      }
      navigate('/patterns')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsSaving(false)
    }
  }

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0]
    if (!selectedFile) {
      return
    }

    setError('')
    setIsUploading(true)

    try {
      const uploadedPath = await patternApi.upload(selectedFile)
      setForm((prev) => ({ ...prev, file: uploadedPath }))
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsUploading(false)
      event.target.value = ''
    }
  }

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>{isEditMode ? 'Modify Violence Pattern' : 'Add Violence Pattern'}</h2>
        <p className="panel-subtitle">
          Fill in the VioPattern fields: name, severity level, threshold, and file.
        </p>

        {error && <p className="banner error">{error}</p>}

        {isLoading ? (
          <p>Loading pattern...</p>
        ) : (
          <form className="grid-form" onSubmit={handleSubmit}>
            <label>
              Name
              <input
                value={form.name}
                onChange={(event) =>
                  setForm((prev) => ({ ...prev, name: event.target.value }))
                }
                required
                maxLength={200}
              />
            </label>

            <label>
              Severity Level
              <input
                type="number"
                value={form.sevLevel}
                min={0}
                onChange={(event) =>
                  setForm((prev) => ({ ...prev, sevLevel: Number(event.target.value) }))
                }
                required
              />
            </label>

            <label>
              Threshold
              <input
                type="number"
                value={form.threshold}
                min={0}
                max={1}
                step="0.01"
                onChange={(event) =>
                  setForm((prev) => ({ ...prev, threshold: Number(event.target.value) }))
                }
                required
              />
            </label>

            <label>
              File Upload
              <input
                type="file"
                accept="video/*"
                onChange={(event) => void handleFileChange(event)}
                disabled={isUploading || isSaving}
              />
            </label>

            <label>
              Uploaded File Path
              <input value={form.file || ''} readOnly placeholder="No file uploaded yet" />
            </label>

            <div className="panel-actions panel-actions-spread">
              <button
                type="button"
                className="secondary-button"
                onClick={() => navigate('/patterns')}
              >
                Back
              </button>
              <button type="submit" className="primary-button" disabled={isSaving || isUploading}>
                {isSaving ? 'Saving...' : isUploading ? 'Uploading...' : isEditMode ? 'Save Changes' : 'Add'}
              </button>
            </div>
          </form>
        )}
      </article>
    </section>
  )
}
