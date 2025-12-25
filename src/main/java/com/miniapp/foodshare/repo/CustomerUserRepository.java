package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Integer> {
    
    /**
     * Tìm user theo email
     */
    Optional<CustomerUser> findByEmail(String email);
    
    /**
     * Tìm user theo provider và providerId
     */
    Optional<CustomerUser> findByProviderAndProviderId(String provider, String providerId);
    
    /**
     * Kiểm tra email đã tồn tại chưa (trừ user hiện tại)
     */
    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<CustomerUser> findByProviderId(String providerId);
    
    /**
     * Tìm users trong phạm vi bán kính từ một điểm tọa độ
     * Sử dụng công thức Haversine để tính khoảng cách chính xác
     * 
     * @param latitude vĩ độ của điểm trung tâm
     * @param longitude kinh độ của điểm trung tâm
     * @param maxDistanceKm bán kính tối đa (km)
     * @return danh sách users trong phạm vi
     */
    @Query(value = """
        SELECT u.*
        FROM Customer_User u
        WHERE u.latitude IS NOT NULL 
          AND u.longitude IS NOT NULL
          AND (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(u.latitude)) * 
                COS(RADIANS(u.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(u.latitude))
            )
          ) <= :maxDistanceKm
        """, nativeQuery = true)
    List<CustomerUser> findUsersWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("maxDistanceKm") Double maxDistanceKm
    );
}