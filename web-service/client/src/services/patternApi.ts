import { http } from './http'
import type {
  ThresholdMap,
  VioPatternRequest,
  VioPatternResponse,
} from '../types/pattern'

interface PatternUploadResponse {
  path: string
}

export const patternApi = {
  async list(): Promise<VioPatternResponse[]> {
    const { data } = await http.get<VioPatternResponse[]>('/api/patterns')
    return data
  },

  async findById(id: number): Promise<VioPatternResponse> {
    const { data } = await http.get<VioPatternResponse>(`/api/patterns/${id}`)
    return data
  },

  async thresholds(): Promise<ThresholdMap> {
    const { data } = await http.get<ThresholdMap>('/api/patterns/thresholds')
    return data
  },

  async create(payload: VioPatternRequest): Promise<VioPatternResponse> {
    const { data } = await http.post<VioPatternResponse>('/api/patterns', payload)
    return data
  },

  async upload(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)

    const { data } = await http.post<PatternUploadResponse>('/api/patterns/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    return data.path
  },

  async update(id: number, payload: VioPatternRequest): Promise<VioPatternResponse> {
    const { data } = await http.put<VioPatternResponse>(`/api/patterns/${id}`, payload)
    return data
  },

  async remove(id: number): Promise<void> {
    await http.delete(`/api/patterns/${id}`)
  },
}
