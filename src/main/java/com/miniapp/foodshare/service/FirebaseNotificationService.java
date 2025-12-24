package com.miniapp.foodshare.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseNotificationService {

    public static void main(String[] args) {
        try {
            // 1. Khởi tạo Firebase Admin SDK
            FileInputStream serviceAccount =
                    new FileInputStream("D:\\pet-project\\miniapp\\be\\foodshare\\src\\main\\resources\\serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            // 2. Định nghĩa Token của thiết bị nhận (Device Token)
            String registrationToken = "dhYqy2D1mAOr3sMDpbDYGc:APA91bEf62UlhJq-BXNOBvvunbXHDfLomYqK_E-emaxqxRdc-2tEt_JAGveVFyT7vwKGMUoiS2TMwIX-mvQeZFbCfo726v2iGZ5cTe3yh6NmAD8NLPVl_lc";

            // 3. Tạo nội dung thông báo
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle("Foodshare")
                            .setBody("Cửa hàng Bún cá rô Thái Bình tại 22 láng hạ đang giảm gía bún riêu cua 30% !!!")
                            .build())
                    .setToken(registrationToken) // Gửi đến token cụ thể
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK") // Dữ liệu bổ sung nếu cần
                    .putData("extra_info", "Gửi từ server Java")
                    .build();

            // 4. Gửi thông báo
            String response = FirebaseMessaging.getInstance().send(message);

            // In ra kết quả
            System.out.println("Gửi thành công! ID tin nhắn: " + response);

        } catch (IOException e) {
            System.err.println("Lỗi đọc file cấu hình: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo: " + e.getMessage());
        }
    }
}