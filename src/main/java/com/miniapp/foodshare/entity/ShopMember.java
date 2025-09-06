package com.miniapp.foodshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Shop_Member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "shop_id", nullable = false)
	private Integer shopId;

	@Column(name = "backoffice_user_id", nullable = false)
	private Integer backofficeUserId;

	@Column(nullable = false, length = 50)
	private String role;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;
} 