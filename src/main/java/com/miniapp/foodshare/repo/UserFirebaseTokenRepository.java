package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.UserFirebaseToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFirebaseTokenRepository extends JpaRepository<UserFirebaseToken, Integer> {
    
    /**
     * Tìm các Firebase tokens theo danh sách userIds và status = "1" (active)
     * 
     * @param userIds danh sách userIds
     * @return danh sách Firebase tokens
     */
    @Query("SELECT uft FROM UserFirebaseToken uft WHERE uft.userId IN :userIds AND uft.status = '1'")
    List<UserFirebaseToken> findByUserIdsAndActive(@Param("userIds") List<Integer> userIds);
}

