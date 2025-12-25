package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.NotifyMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NotifyMessageRepository extends JpaRepository<NotifyMessage, Integer> {
    
    /**
     * Tìm các notify messages theo ngày với status = 0, phân trang
     * 
     * @param date ngày cần tìm
     * @param status trạng thái (0 - chờ xử lý)
     * @param pageable phân trang
     * @return danh sách notify messages
     */
    @Query("SELECT nm FROM NotifyMessage nm WHERE DATE(nm.createdAt) = :date AND nm.status = :status ORDER BY nm.id ASC")
    Page<NotifyMessage> findByDateAndStatus(@Param("date") LocalDate date, @Param("status") String status, Pageable pageable);
    
    /**
     * Tìm các notify messages theo templateId và status
     * 
     * @param templateId ID của template
     * @param status trạng thái
     * @return danh sách notify messages
     */
    List<NotifyMessage> findByTemplateIdAndStatus(Integer templateId, String status);
}
