package com.miniapp.foodshare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.SendResponse;
import com.miniapp.foodshare.entity.NotifyMessage;
import com.miniapp.foodshare.entity.NotifyTemplate;
import com.miniapp.foodshare.entity.UserFirebaseToken;
import com.miniapp.foodshare.repo.NotifyMessageRepository;
import com.miniapp.foodshare.repo.NotifyTemplateRepository;
import com.miniapp.foodshare.repo.UserFirebaseTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyMessageScheduler {

    private static final int BATCH_SIZE = 100; // Số lượng bản ghi mỗi lần xử lý

    private final NotifyMessageRepository notifyMessageRepository;
    private final NotifyTemplateRepository notifyTemplateRepository;
    private final UserFirebaseTokenRepository userFirebaseTokenRepository;
    private final FirebaseNotificationService firebaseNotificationService;
    private final ObjectMapper objectMapper;

    /**
     * Cron job chạy mỗi 5 phút để xử lý notify messages
     * Format: fixedDelayString = "PT5M" (5 minutes)
     */
    @Scheduled(fixedDelayString = "PT5M")
    @Transactional
    public void processNotifyMessages() {
        log.info("Starting scheduled task to process notify messages");
        
        try {
            LocalDate today = LocalDate.now();
            int page = 0;
            boolean hasMore = true;

            // Xử lý từng lô 100 bản ghi
            while (hasMore) {
                Page<NotifyMessage> messagePage = notifyMessageRepository.findByDateAndStatus(
                        today, 
                        "0", // status = 0 (chờ xử lý)
                        PageRequest.of(page, BATCH_SIZE)
                );

                List<NotifyMessage> messages = messagePage.getContent();

                if (messages.isEmpty()) {
                    hasMore = false;
                    log.info("No more messages to process");
                    break;
                }

                log.info("Processing batch {}: {} messages", page + 1, messages.size());

                // Group messages theo templateId
                Map<Integer, List<NotifyMessage>> messagesByTemplate = messages.stream()
                        .collect(Collectors.groupingBy(NotifyMessage::getTemplateId));

                // Xử lý từng group
                for (Map.Entry<Integer, List<NotifyMessage>> entry : messagesByTemplate.entrySet()) {
                    Integer templateId = entry.getKey();
                    List<NotifyMessage> templateMessages = entry.getValue();

                    processTemplateMessages(templateId, templateMessages);
                }

                // Kiểm tra còn bản ghi không
                hasMore = messagePage.hasNext();
                page++;
            }

            log.info("Scheduled task completed successfully");

        } catch (Exception e) {
            log.error("Error in scheduled task: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý messages của một template
     */
    private void processTemplateMessages(Integer templateId, List<NotifyMessage> messages) {
        try {
            // Lấy template
            Optional<NotifyTemplate> templateOpt = notifyTemplateRepository.findById(templateId);
            if (templateOpt.isEmpty()) {
                log.warn("Template not found: templateId={}", templateId);
                updateMessagesStatus(messages, "2"); // failed
                return;
            }

            NotifyTemplate template = templateOpt.get();

            // Cập nhật trạng thái template: 0 -> 1 (đang xử lý)
            if ("0".equals(template.getStatus())) {
                template.setStatus("1");
                template.setUpdatedAt(LocalDateTime.now());
                notifyTemplateRepository.save(template);
                log.info("Template status updated to processing: templateId={}", templateId);
            }

            // Lấy danh sách userIds
            List<Integer> userIds = messages.stream()
                    .map(NotifyMessage::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            // Lấy Firebase tokens của các users
            List<UserFirebaseToken> tokens = userFirebaseTokenRepository.findByUserIdsAndActive(userIds);

            if (tokens.isEmpty()) {
                log.warn("No active Firebase tokens found for template: templateId={}, userIds={}", 
                        templateId, userIds);
                updateMessagesStatus(messages, "2"); // failed
                updateTemplateStatus(templateId, "2"); // đã xử lý
                return;
            }

            // Chuẩn bị data từ metadata
            Map<String, String> dataMap = new HashMap<>();
            if (template.getMetadata() != null && !template.getMetadata().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> metadataObj = objectMapper.readValue(
                            template.getMetadata(), 
                            Map.class
                    );
                    // Put toàn bộ metadata vào data với key "data"
                    String dataJson = objectMapper.writeValueAsString(metadataObj);
                    dataMap.put("data", dataJson);
                } catch (Exception e) {
                    log.warn("Error parsing metadata for template: templateId={}, error={}", 
                            templateId, e.getMessage());
                }
            }

            // Gửi notification
            List<String> allTokens = tokens.stream()
                    .map(UserFirebaseToken::getFirebaseToken)
                    .collect(Collectors.toList());

            BatchResponse response = firebaseNotificationService.sendBatchNotification(
                    allTokens,
                    "Foodshare",
                    template.getContent() != null ? template.getContent() : "",
                    dataMap
            );

            // Cập nhật trạng thái messages dựa trên kết quả
            if (response != null) {
                List<SendResponse> responses = response.getResponses();
                int successCount = 0;
                int failureCount = 0;
                List<UserFirebaseToken> invalidTokens = new ArrayList<>();

                // Map token index to UserFirebaseToken để có thể xóa token không hợp lệ
                Map<Integer, UserFirebaseToken> tokenIndexToToken = new HashMap<>();
                int tokenIndex = 0;
                for (UserFirebaseToken token : tokens) {
                    tokenIndexToToken.put(tokenIndex, token);
                    tokenIndex++;
                }

                // Map token index to userId
                Map<Integer, Integer> tokenIndexToUserId = new HashMap<>();
                tokenIndex = 0;
                for (UserFirebaseToken token : tokens) {
                    tokenIndexToUserId.put(tokenIndex, token.getUserId());
                    tokenIndex++;
                }

                // Map userId -> list of token indices (một user có thể có nhiều tokens)
                Map<Integer, List<Integer>> userIdToTokenIndices = new HashMap<>();
                for (Map.Entry<Integer, Integer> entry : tokenIndexToUserId.entrySet()) {
                    Integer index = entry.getKey();
                    Integer userId = entry.getValue();
                    userIdToTokenIndices.computeIfAbsent(userId, k -> new ArrayList<>()).add(index);
                }

                // Kiểm tra và xóa các token không hợp lệ
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse sendResponse = responses.get(i);
                    if (!sendResponse.isSuccessful() && sendResponse.getException() != null) {
                        // Lấy error code từ exception
                        String errorCode = null;
                        try {
                            FirebaseMessagingException exception = sendResponse.getException();
                            if (exception.getMessagingErrorCode() != null) {
                                errorCode = exception.getMessagingErrorCode().toString();
                            } else {
                                // Fallback: lấy từ message
                                String message = exception.getMessage();
                                if (message != null) {
                                    errorCode = message.toUpperCase();
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error getting error code from exception: {}", e.getMessage());
                        }
                        
                        // Kiểm tra các lỗi token không hợp lệ
                        if (isInvalidTokenError(errorCode)) {
                            UserFirebaseToken invalidToken = tokenIndexToToken.get(i);
                            if (invalidToken != null) {
                                invalidTokens.add(invalidToken);
                                log.warn("Invalid token detected and will be deleted: tokenId={}, userId={}, errorCode={}", 
                                        invalidToken.getId(), invalidToken.getUserId(), errorCode);
                            }
                        }
                    }
                }

                // Xóa các token không hợp lệ
                if (!invalidTokens.isEmpty()) {
                    userFirebaseTokenRepository.deleteAll(invalidTokens);
                    log.info("Deleted {} invalid Firebase tokens", invalidTokens.size());
                }

                // Cập nhật từng message dựa trên kết quả
                for (NotifyMessage message : messages) {
                    Integer userId = message.getUserId();
                    List<Integer> tokenIndices = userIdToTokenIndices.get(userId);

                    if (tokenIndices == null || tokenIndices.isEmpty()) {
                        // User không có token, đánh dấu failed
                        message.setStatus("2"); // failed
                        message.setUpdatedAt(LocalDateTime.now());
                        failureCount++;
                    } else {
                        // Kiểm tra xem có ít nhất một token thành công không
                        boolean hasSuccess = false;
                        for (Integer index : tokenIndices) {
                            if (index < responses.size() && responses.get(index).isSuccessful()) {
                                hasSuccess = true;
                                break;
                            }
                        }

                        if (hasSuccess) {
                            message.setStatus("1"); // đã gửi
                            successCount++;
                        } else {
                            message.setStatus("2"); // failed
                            failureCount++;
                        }
                        message.setUpdatedAt(LocalDateTime.now());
                    }
                }

                log.info("Notification sent for template: templateId={}, success={}, failure={}, invalidTokensDeleted={}", 
                        templateId, successCount, failureCount, invalidTokens.size());
            } else {
                // Nếu response null, đánh dấu tất cả failed
                log.warn("Failed to send notification for template: templateId={}", templateId);
                updateMessagesStatus(messages, "2"); // failed
            }

            // Lưu tất cả messages
            notifyMessageRepository.saveAll(messages);

            // Cập nhật trạng thái template: 1 -> 2 (đã xử lý)
            updateTemplateStatus(templateId, "2");

        } catch (Exception e) {
            log.error("Error processing template messages: templateId={}, error={}", 
                    templateId, e.getMessage(), e);
            updateMessagesStatus(messages, "2"); // failed
            updateTemplateStatus(templateId, "2"); // đã xử lý
        }
    }

    /**
     * Cập nhật trạng thái của messages
     */
    private void updateMessagesStatus(List<NotifyMessage> messages, String status) {
        LocalDateTime now = LocalDateTime.now();
        for (NotifyMessage message : messages) {
            message.setStatus(status);
            message.setUpdatedAt(now);
        }
        notifyMessageRepository.saveAll(messages);
    }

    /**
     * Cập nhật trạng thái của template
     */
    private void updateTemplateStatus(Integer templateId, String status) {
        Optional<NotifyTemplate> templateOpt = notifyTemplateRepository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotifyTemplate template = templateOpt.get();
            template.setStatus(status);
            template.setUpdatedAt(LocalDateTime.now());
            notifyTemplateRepository.save(template);
            log.info("Template status updated: templateId={}, status={}", templateId, status);
        }
    }

    /**
     * Kiểm tra xem lỗi có phải là token không hợp lệ không
     * 
     * @param errorCode mã lỗi từ Firebase
     * @return true nếu là lỗi token không hợp lệ
     */
    private boolean isInvalidTokenError(String errorCode) {
        if (errorCode == null) {
            return false;
        }
        
        // Các error code cho token không hợp lệ
        return errorCode.equals("INVALID_ARGUMENT") ||
               errorCode.equals("UNREGISTERED") ||
               errorCode.equals("INVALID_REGISTRATION_TOKEN") ||
               errorCode.contains("INVALID") ||
               errorCode.contains("UNREGISTERED");
    }
}

