package com.miniapp.foodshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
	name = "Customer_User",
	uniqueConstraints = {
		@UniqueConstraint(name = "email", columnNames = {"email"}),
		@UniqueConstraint(name = "provider_id", columnNames = {"provider_id"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false, length = 255, unique = true)
	private String email;

	@Column(nullable = false, length = 50)
	private String provider;

	@Column(name = "provider_id", nullable = false, length = 255, unique = true)
	private String providerId;

	@Column(name = "profile_picture_url", length = 255)
	private String profilePictureUrl;

	@Column(name = "phone_number", length = 20)
	private String phoneNumber;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;
} 