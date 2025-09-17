package com.miniapp.foodshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 255, message = "Tên phải có từ 2 đến 255 ký tự")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phoneNumber;

    @Size(max = 255, message = "URL ảnh đại diện không được quá 255 ký tự")
    private String profilePictureUrl;
}
