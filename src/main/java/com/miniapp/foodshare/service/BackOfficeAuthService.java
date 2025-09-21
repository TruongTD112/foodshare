package com.miniapp.foodshare.service;

import com.miniapp.foodshare.common.ErrorCode;
import com.miniapp.foodshare.common.Result;
import com.miniapp.foodshare.common.UserRole;
import com.miniapp.foodshare.dto.BackOfficeAuthResponse;
import com.miniapp.foodshare.dto.CreateUserRequest;
import com.miniapp.foodshare.dto.UpdateAdminRequest;
import com.miniapp.foodshare.dto.SellerRegisterRequest;
import com.miniapp.foodshare.dto.UnifiedLoginRequest;
import com.miniapp.foodshare.entity.BackOfficeUser;
import com.miniapp.foodshare.repo.BackOfficeUserRepository;
import com.miniapp.foodshare.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackOfficeAuthService {

    private final BackOfficeUserRepository backOfficeUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Đăng nhập (Admin/Seller)
     */
    @Transactional(readOnly = true)
    public Result<BackOfficeAuthResponse> login(UnifiedLoginRequest request) {
        try {
            // Tìm user theo email
            Optional<BackOfficeUser> userOptional = backOfficeUserRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("User not found: {}", request.getEmail());
                return Result.error(ErrorCode.USER_NOT_FOUND, "Email hoặc mật khẩu không đúng");
            }

            BackOfficeUser user = userOptional.get();

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.warn("Invalid password for user: {}", request.getEmail());
                return Result.error(ErrorCode.INVALID_CREDENTIALS, "Email hoặc mật khẩu không đúng");
            }

            // Tạo JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", user.getId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().getCode());
            claims.put("type", "backoffice");
            String accessToken = JwtService.generateToken("user:" + user.getId(), claims);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Token hết hạn sau 24h

            BackOfficeAuthResponse response = BackOfficeAuthResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .accessToken(accessToken)
                    .expiresAt(expiresAt)
                    .tokenType("Bearer")
                    .message("Đăng nhập thành công")
                    .build();

            log.info("User logged in successfully: userId={}, email={}, role={}", 
                    user.getId(), user.getEmail(), user.getRole());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error during login for email={}", request.getEmail(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi đăng nhập");
        }
    }

    /**
     * Seller đăng ký
     */
    @Transactional
    public Result<BackOfficeAuthResponse> registerSeller(SellerRegisterRequest request) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (backOfficeUserRepository.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                return Result.error(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
            }

            // Tạo seller mới
            BackOfficeUser user = BackOfficeUser.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(UserRole.SELLER) // Mặc định là SELLER
                    .build();

            BackOfficeUser savedUser = backOfficeUserRepository.save(user);

            BackOfficeAuthResponse response = BackOfficeAuthResponse.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole())
                    .message("Đăng ký seller thành công")
                    .build();

            log.info("Seller registered successfully: userId={}, email={}, role={}", 
                    savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error registering seller: email={}", request.getEmail(), e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi đăng ký seller");
        }
    }

    /**
     * Lấy thông tin user hiện tại
     */
    @Transactional(readOnly = true)
    public Result<BackOfficeAuthResponse> getCurrentUser(Integer userId) {
        try {
            Optional<BackOfficeUser> userOptional = backOfficeUserRepository.findById(userId);
            if (userOptional.isEmpty()) {
                log.warn("User not found: userId={}", userId);
                return Result.error(ErrorCode.USER_NOT_FOUND, "Không tìm thấy user");
            }

            BackOfficeUser user = userOptional.get();

            BackOfficeAuthResponse response = BackOfficeAuthResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .accessToken(null) // Không trả về token khi get current user
                    .tokenType(null)
                    .expiresAt(null)
                    .message("Lấy thông tin user thành công")
                    .build();

            log.info("Current user info retrieved: userId={}, email={}, role={}", 
                    user.getId(), user.getEmail(), user.getRole());
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error getting current user: userId={}", userId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi lấy thông tin user");
        }
    }

    /**
     * Admin tạo admin mới (chỉ admin mới có thể tạo)
     */
    @Transactional
    public Result<BackOfficeAuthResponse> createAdmin(CreateUserRequest request, Integer adminId) {
        try {
            // Kiểm tra admin có tồn tại và có quyền không
            Optional<BackOfficeUser> adminOptional = backOfficeUserRepository.findById(adminId);
            if (adminOptional.isEmpty() || !adminOptional.get().getRole().isAdmin()) {
                log.warn("Unauthorized admin creation attempt: adminId={}", adminId);
                return Result.error(ErrorCode.FORBIDDEN, "Chỉ admin mới có thể tạo admin mới");
            }

            // Kiểm tra role phải là ADMIN
            if (!UserRole.ADMIN.getCode().equals(request.getRole())) {
                log.warn("Invalid role for admin creation: role={}", request.getRole());
                return Result.error(ErrorCode.INVALID_REQUEST, "Chỉ có thể tạo admin");
            }

            // Kiểm tra email đã tồn tại chưa
            if (backOfficeUserRepository.existsByEmail(request.getEmail())) {
                log.warn("Email already exists: {}", request.getEmail());
                return Result.error(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
            }

            // Tạo admin mới
            BackOfficeUser user = BackOfficeUser.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(UserRole.ADMIN)
                    .build();

            BackOfficeUser savedUser = backOfficeUserRepository.save(user);

            // Tạo JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", savedUser.getId());
            claims.put("email", savedUser.getEmail());
            claims.put("role", savedUser.getRole().getCode());
            claims.put("type", "backoffice");
            String accessToken = JwtService.generateToken("user:" + savedUser.getId(), claims);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Token hết hạn sau 24h

            BackOfficeAuthResponse response = BackOfficeAuthResponse.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole())
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresAt(expiresAt)
                    .message("Tạo admin thành công")
                    .build();

            log.info("Admin created by admin: userId={}, email={}, role={}, adminId={}", 
                    savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), adminId);
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error creating admin: email={}, adminId={}", request.getEmail(), adminId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi tạo admin mới");
        }
    }

    /**
     * Cập nhật admin
     */
    @Transactional
    public Result<BackOfficeAuthResponse> updateAdmin(Integer adminId, UpdateAdminRequest request, Integer currentAdminId) {
        try {
            // Kiểm tra admin hiện tại có quyền không
            Optional<BackOfficeUser> currentAdminOptional = backOfficeUserRepository.findById(currentAdminId);
            if (currentAdminOptional.isEmpty() || !currentAdminOptional.get().getRole().isAdmin()) {
                log.warn("Unauthorized admin update attempt: currentAdminId={}", currentAdminId);
                return Result.error(ErrorCode.FORBIDDEN, "Chỉ admin mới có thể cập nhật admin");
            }

            // Kiểm tra admin cần cập nhật có tồn tại không
            Optional<BackOfficeUser> adminOptional = backOfficeUserRepository.findById(adminId);
            if (adminOptional.isEmpty()) {
                log.warn("Admin not found: adminId={}", adminId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy admin");
            }

            BackOfficeUser admin = adminOptional.get();

            // Cập nhật các trường không null
            if (request.getName() != null) admin.setName(request.getName());
            if (request.getEmail() != null) {
                // Kiểm tra email đã tồn tại chưa (trừ chính admin này)
                if (backOfficeUserRepository.existsByEmailAndIdNot(request.getEmail(), adminId)) {
                    log.warn("Email already exists: {}", request.getEmail());
                    return Result.error(ErrorCode.EMAIL_ALREADY_EXISTS, "Email đã tồn tại");
                }
                admin.setEmail(request.getEmail());
            }
            if (request.getPassword() != null) {
                admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }

            BackOfficeUser updatedAdmin = backOfficeUserRepository.save(admin);

            BackOfficeAuthResponse response = BackOfficeAuthResponse.builder()
                    .id(updatedAdmin.getId())
                    .name(updatedAdmin.getName())
                    .email(updatedAdmin.getEmail())
                    .role(updatedAdmin.getRole())
                    .accessToken(null) // Không trả về token khi update
                    .tokenType(null)
                    .expiresAt(null)
                    .message("Cập nhật admin thành công")
                    .build();

            log.info("Admin updated by admin: adminId={}, email={}, currentAdminId={}", 
                    updatedAdmin.getId(), updatedAdmin.getEmail(), currentAdminId);
            return Result.success(response);

        } catch (Exception e) {
            log.error("Error updating admin: adminId={}, currentAdminId={}", adminId, currentAdminId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi cập nhật admin");
        }
    }

    /**
     * Xóa admin
     */
    @Transactional
    public Result<String> deleteAdmin(Integer adminId, Integer currentAdminId) {
        try {
            // Kiểm tra admin hiện tại có quyền không
            Optional<BackOfficeUser> currentAdminOptional = backOfficeUserRepository.findById(currentAdminId);
            if (currentAdminOptional.isEmpty() || !currentAdminOptional.get().getRole().isAdmin()) {
                log.warn("Unauthorized admin deletion attempt: currentAdminId={}", currentAdminId);
                return Result.error(ErrorCode.FORBIDDEN, "Chỉ admin mới có thể xóa admin");
            }

            // Không cho phép xóa chính mình
            if (adminId.equals(currentAdminId)) {
                log.warn("Admin cannot delete themselves: adminId={}", adminId);
                return Result.error(ErrorCode.FORBIDDEN, "Không thể xóa chính mình");
            }

            // Kiểm tra admin cần xóa có tồn tại không
            Optional<BackOfficeUser> adminOptional = backOfficeUserRepository.findById(adminId);
            if (adminOptional.isEmpty()) {
                log.warn("Admin not found: adminId={}", adminId);
                return Result.error(ErrorCode.NOT_FOUND, "Không tìm thấy admin");
            }

            backOfficeUserRepository.deleteById(adminId);

            log.info("Admin deleted by admin: adminId={}, currentAdminId={}", adminId, currentAdminId);
            return Result.success("Xóa admin thành công");

        } catch (Exception e) {
            log.error("Error deleting admin: adminId={}, currentAdminId={}", adminId, currentAdminId, e);
            return Result.error(ErrorCode.INTERNAL_ERROR, "Lỗi khi xóa admin");
        }
    }
}
