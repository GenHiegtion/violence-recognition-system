export interface VioPatternRequest {
  name: string
  sevLevel: number
  threshold: number
  file?: string
}

export interface VioPatternResponse {
  id: number
  name: string
  sevLevel: number
  threshold: number
  file?: string
}

export type ThresholdMap = Record<string, number>
