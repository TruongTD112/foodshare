package com.miniapp.foodshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateAdminRequest {
    @Size(max = 255, message = "Tên không được vượt quá 255 ký tự")
    String name;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    String email;

    @Size(min = 6, max = 50, message = "Mật khẩu phải có từ 6 đến 50 ký tự")
    String password;
}
