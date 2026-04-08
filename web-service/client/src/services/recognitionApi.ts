import { http } from './http'
import type {
  ExecuteRecognitionRequest,
  RecognitionModel,
  RecognitionResult,
  RecognitionUploadResponse,
} from '../types/recognition'

export const recognitionApi = {
  async uploadVideo(file: File, fps = 5): Promise<RecognitionUploadResponse> {
    const formData = new FormData()
    formData.append('file', file)

    const { data } = await http.post<RecognitionUploadResponse>('/api/recognitions/upload', formData, {
      params: { fps },
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return data
  },

  async listModels(name?: string): Promise<RecognitionModel[]> {
    const { data } = await http.get<RecognitionModel[]>('/api/models', {
      params: {
        ...(name ? { name } : {}),
      },
    })
    return data
  },

  async executeRecognition(payload: ExecuteRecognitionRequest): Promise<RecognitionResult> {
    const { data } = await http.post<RecognitionResult>('/api/recognitions/execute', payload, {
      timeout: 180000,
    })
    return data
  },
}
