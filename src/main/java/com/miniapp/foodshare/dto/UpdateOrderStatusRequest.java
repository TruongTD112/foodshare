package com.miniapp.foodshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(1|2|3|4)$",
            message = "Trạng thái phải là 1 (PENDING), 2 (CONFIRMED), 3 (CANCELLED), hoặc 4 (COMPLETED)")
    @io.swagger.v3.oas.annotations.media.Schema(
            description = "Trạng thái đơn hàng: 1 (PENDING), 2 (CONFIRMED), 3 (CANCELLED), 4 (COMPLETED)",
            example = "1",
            allowableValues = {"1", "2", "3", "4"}
    )
    String status;
}
