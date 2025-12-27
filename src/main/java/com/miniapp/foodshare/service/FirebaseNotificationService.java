package com.miniapp.foodshare.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FirebaseNotificationService {

    @Value("${firebaseConfigJson}")
    private String firebaseConfigJson;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Chuyển đổi String JSON thành InputStream
                InputStream serviceAccountStream = new ByteArrayInputStream(
                        firebaseConfigJson.getBytes(StandardCharsets.UTF_8)
                );

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully from JSON string");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi batch notification với title, body và data tùy chỉnh
     *
     * @param registrationTokens danh sách Firebase tokens
     * @param title              tiêu đề thông báo
     * @param body               nội dung thông báo
     * @param dataMap            dữ liệu bổ sung (metadata)
     * @return BatchResponse chứa kết quả gửi
     */
    public BatchResponse sendBatchNotification(List<String> registrationTokens, String title, String body, Map<String, String> dataMap) {
        try {
            if (registrationTokens == null || registrationTokens.isEmpty()) {
                log.warn("No registration tokens provided");
                return null;
            }

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(registrationTokens);

            // Thêm data (bắt buộc)
            messageBuilder.putData("title", title);
            messageBuilder.putData("ok", title);
            messageBuilder.putData("body", body);

            // Thêm data động (link, type, id...)
            if (dataMap != null && !dataMap.isEmpty()) {
                for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                    messageBuilder.putData(entry.getKey(), entry.getValue());
                }
            }

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