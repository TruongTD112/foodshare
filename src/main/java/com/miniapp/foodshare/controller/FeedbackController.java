package com.miniapp.foodshare.controller;

import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.dto.FeedbackRequest;
import com.miniapp.foodshare.dto.FeedbackResponse;
import com.miniapp.foodshare.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/feedback")
@Tag(name = "Feedback", description = "API quản lý feedback của người dùng")
public class FeedbackController {
    
    private final FeedbackService feedbackService;
    
    @PostMapping
    @Operation(
            summary = "Tạo feedback mới",
            description = "Người dùng gửi feedback về ứng dụng (không cần đăng nhập)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo feedback thành công",
                    content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public Result<FeedbackResponse> createFeedback(
            @Valid @RequestBody FeedbackRequest request
    ) {
        log.info("Create feedback request: userId={}, contentLength={}", 
                request.getUserId(), request.getContent() != null ? request.getContent().length() : 0);
        
        Result<FeedbackResponse> result = feedbackService.createFeedback(request);
        
        if (result.isSuccess()) {
            log.info("Feedback created successfully: id={}, userId={}", 
                    result.getData().getId(), result.getData().getUserId());
        } else {
            log.warn("Feedback creation failed: code={}, message={}", 
                    result.getCode(), result.getMessage());
        }
        
        return result;
    }
}
