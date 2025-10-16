package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.FeedbackRequest;
import com.miniapp.foodshare.dto.FeedbackResponse;
import com.miniapp.foodshare.entity.Feedback;
import com.miniapp.foodshare.repo.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    /**
     * Tạo feedback mới
     */
    @Transactional
    public Result<FeedbackResponse> createFeedback(FeedbackRequest request) {
        try {
            if (request == null) {
                log.warn("Invalid request: request is null");
                return Result.error(ErrorCode.INVALID_REQUEST, "Invalid request");
            }
            
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                log.warn("Invalid request: content is empty");
                return Result.error(ErrorCode.INVALID_REQUEST, "Nội dung feedback không được để trống");
            }
            
            // Tạo feedback entity
            Feedback feedback = Feedback.builder()
                    .content(request.getContent().trim())
                    .userId(request.getUserId()) // Có thể null nếu không có user
                    .build();
            
            // Lưu vào database
            Feedback savedFeedback = feedbackRepository.save(feedback);
            
            // Map sang response
            FeedbackResponse response = FeedbackResponse.builder()
                    .id(savedFeedback.getId())
                    .content(savedFeedback.getContent())
                    .userId(savedFeedback.getUserId())
                    .createdAt(savedFeedback.getCreatedAt())
                    .build();
            
            log.info("Feedback created successfully: id={}, userId={}, contentLength={}", 
                    response.getId(), response.getUserId(), response.getContent().length());
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("Error creating feedback: userId={}, content={}", 
                    request.getUserId(), request.getContent(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo feedback");
        }
    }
}
