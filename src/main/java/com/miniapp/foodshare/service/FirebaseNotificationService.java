package com.miniapp.foodshare.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FirebaseNotificationService {

    @Value("classpath:serviceAccountKey.json")
    private Resource serviceAccountResource;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountResource.getInputStream()))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi batch notification với title, body và data tùy chỉnh
     * 
     * @param registrationTokens danh sách Firebase tokens
     * @param title tiêu đề thông báo
     * @param body nội dung thông báo
     * @param dataMap dữ liệu bổ sung (metadata)
     * @return BatchResponse chứa kết quả gửi
     */
    public BatchResponse sendBatchNotification(List<String> registrationTokens, String title, String body, Map<String, String> dataMap) {
        try {
            if (registrationTokens == null || registrationTokens.isEmpty()) {
                log.warn("No registration tokens provided");
                return null;
            }

            // Tạo MulticastMessage với title, body và data
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .addAllTokens(registrationTokens);

            // Thêm data nếu có
            if (dataMap != null && !dataMap.isEmpty()) {
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    messageBuilder.putData(entry.getKey(), entry.getValue());
                }
            }

            // Thêm click_action mặc định
            messageBuilder.putData("click_action", "FLUTTER_NOTIFICATION_CLICK");

            MulticastMessage message = messageBuilder.build();

            // Gửi lô thông báo
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            // Xử lý kết quả
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.warn("Token failed: {}, reason: {}", 
                                registrationTokens.get(i), 
                                responses.get(i).getException() != null ? 
                                        responses.get(i).getException().getMessage() : "Unknown error");
                    }
                }
            }

            log.info("Batch notification sent: success={}, failure={}, total={}", 
                    response.getSuccessCount(), response.getFailureCount(), registrationTokens.size());

            return response;

        } catch (Exception e) {
            log.error("Error sending batch notification: {}", e.getMessage(), e);
            return null;
        }
    }
}