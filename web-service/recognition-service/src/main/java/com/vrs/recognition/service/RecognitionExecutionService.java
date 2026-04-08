package com.vrs.recognition.service;

import com.vrs.recognition.dto.request.RecognitionExecuteRequest;
import com.vrs.recognition.dto.response.RecognitionUploadResponse;
import com.vrs.recognition.dto.response.RecognitionResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface RecognitionExecutionService {

    RecognitionUploadResponse uploadVideo(MultipartFile file, int fps, Integer durationSeconds);

    RecognitionResponse executeRecognition(RecognitionExecuteRequest request);

    List<RecognitionResponse> findAll();

    RecognitionResponse findById(Long recognitionId);
}
