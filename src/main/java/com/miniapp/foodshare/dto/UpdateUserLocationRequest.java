package com.miniapp.foodshare.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateUserLocationRequest {
    @DecimalMin(value = "-90.0", message = "Latitude phải từ -90 đến 90")
    @DecimalMax(value = "90.0", message = "Latitude phải từ -90 đến 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude phải từ -180 đến 180")
    @DecimalMax(value = "180.0", message = "Longitude phải từ -180 đến 180")
    private BigDecimal longitude;
}

