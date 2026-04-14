import { useRef, useState } from 'react'
import type { ChangeEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { getErrorMessage } from '../services/http'
import { recognitionApi } from '../services/recognitionApi'
import type { RecognitionWorkflowState } from '../types/recognition'

interface RecognitionRouteState {
  workflow?: RecognitionWorkflowState
}

export function RecognitionVideoSelectionPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAdmin } = useAuth()
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const routeState = location.state as RecognitionRouteState | null
  const initialWorkflow = routeState?.workflow

  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [workflow, setWorkflow] = useState<RecognitionWorkflowState | null>(initialWorkflow ?? null)
  const [isUploading, setIsUploading] = useState(false)
  const [error, setError] = useState('')

  const handlePickVideo = () => {
    fileInputRef.current?.click()
  }

  const handleVideoChange = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (!file) {
      return
    }

    const previewUrl = URL.createObjectURL(file)
    setSelectedFile(file)
    setWorkflow({
      selectedFileName: file.name,
      previewUrl,
    })
    setError('')
  }

  const handleUpload = async () => {
    setError('')

    if (!selectedFile) {
      if (workflow?.uploadedSource) {
        navigate('/recognition/models', { state: { workflow } })
        return
      }
      setError('Please choose a video before uploading.')
      return
    }

    setIsUploading(true)
    try {
      const response = await recognitionApi.uploadVideo(selectedFile)
      const nextWorkflow: RecognitionWorkflowState = {
        selectedFileName: selectedFile.name,
        previewUrl: workflow?.previewUrl ?? '',
        uploadId: response.uploadId,
        uploadedSource: response.source,
      }
      setWorkflow(nextWorkflow)
      navigate('/recognition/models', { state: { workflow: nextWorkflow } })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setIsUploading(false)
    }
  }

  return (
    <section className="simple-page">
      <article className="panel">
        <h2>Video Selection</h2>
        <p className="panel-subtitle">
          Select the video to check whether it contains violent behavior.
        </p>

        {error && <p className="banner error">{error}</p>}

        <input
          ref={fileInputRef}
          type="file"
          accept="video/*"
          onChange={handleVideoChange}
          className="hidden-file-input"
        />

        <div className="panel-actions">
          <button type="button" className="secondary-button" onClick={handlePickVideo}>
            Select Video
          </button>
          <button
            type="button"
            className="primary-button"
            onClick={() => void handleUpload()}
            disabled={isUploading}
          >
            {isUploading ? 'Uploading...' : 'Upload'}
          </button>
        </div>

        <section className="media-panel recognition-media-panel">
          <h3>Selected Video</h3>
          {workflow?.previewUrl ? (
            <>
              <video className="detail-video" controls preload="metadata" src={workflow.previewUrl}>
                Your browser does not support video playback.
              </video>
              <p className="muted-text">{workflow.selectedFileName}</p>
            </>
          ) : (
            <p className="muted-text">No video selected yet.</p>
          )}
        </section>

        <div className="panel-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => navigate(isAdmin ? '/manager-home' : '/user-home')}
          >
            Back
          </button>
        </div>
      </article>
    </section>
  )
}
