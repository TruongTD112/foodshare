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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Shop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 255)
	private String address;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "image_url", length = 255)
	private String imageUrl;

	@Column(precision = 10, scale = 8)
	private BigDecimal latitude;

	@Column(precision = 11, scale = 8)
	private BigDecimal longitude;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(precision = 3, scale = 2)
	private BigDecimal rating;

	@Column(name = "status")
	private String status;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;
} 