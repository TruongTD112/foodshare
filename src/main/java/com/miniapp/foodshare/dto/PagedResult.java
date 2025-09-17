package com.miniapp.foodshare.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO đơn giản cho kết quả phân trang, chỉ chứa những thông tin cần thiết
 */
@Value
@Builder
public class PagedResult<T> {
    List<T> content;        // Danh sách dữ liệu
    int page;               // Trang hiện tại (0-based)
    int size;               // Kích thước trang
    long totalElements;     // Tổng số phần tử
    int totalPages;         // Tổng số trang
    boolean hasNext;        // Có trang tiếp theo không
    boolean hasPrevious;    // Có trang trước không
}
