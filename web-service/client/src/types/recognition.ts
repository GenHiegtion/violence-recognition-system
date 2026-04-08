export interface RecognitionUploadResponse {
  uploadId: string
  source: string
  fps: number
  estimatedFrames: number
  status: string
  createdAt: string
}

export interface RecognitionModel {
  id: number
  name: string
  pt: string
}

export interface RecognitionResult {
  id: number
  result: string
  file: string
  date: string
  confidenceScore: number
  userId: number | null
  modelId: number | null
  modelName: string | null
  vioPatternId: number | null
}

export interface ExecuteRecognitionRequest {
  source: string
  sourceId?: string
  modelId: number
  userId?: number
  vioPatternId?: number
  patternCode?: string
  framesCount?: number
}

export interface RecognitionWorkflowState {
  selectedFileName: string
  previewUrl: string
  uploadId?: string
  uploadedSource?: string
  selectedModel?: RecognitionModel
  recognitionResult?: RecognitionResult
}
