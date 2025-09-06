package com.miniapp.foodshare.repo;

import com.miniapp.foodshare.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerUserRepository extends JpaRepository<CustomerUser, Integer> {
	Optional<CustomerUser> findByEmail(String email);
	Optional<CustomerUser> findByProviderId(String providerId);
	boolean existsByEmail(String email);
	boolean existsByProviderId(String providerId);
} 