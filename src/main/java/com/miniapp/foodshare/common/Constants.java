package com.miniapp.foodshare.common;

/**
 * Application constants used throughout the application
 */
public final class Constants {
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Pagination constants
     */
    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int DEFAULT_PAGE_NUMBER = 0;
        
        private Pagination() {}
    }
    
    /**
     * Order status constants
     */
    public static final class OrderStatus {
        public static final String PENDING = "1";
        public static final String CONFIRMED = "2";
        public static final String COMPLETED = "4";
        public static final String CANCELLED = "3";
        
        private OrderStatus() {}
    }
    
    /**
     * Product status constants
     */
    public static final class ProductStatus {
        public static final String ACTIVE = "1";
        public static final String INACTIVE = "0";
        
        private ProductStatus() {}
    }
    
    /**
     * Shop status constants
     */
    public static final class ShopStatus {
        public static final String ACTIVE = "1";
        public static final String INACTIVE = "0";
        
        private ShopStatus() {}
    }
    
    /**
     * Authentication constants
     */
    public static final class Auth {
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final int TOKEN_EXPIRY_HOURS = 24;
        
        private Auth() {}
    }
    
    /**
     * File upload constants
     */
    public static final class FileUpload {
        public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
        public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif"};
        public static final String UPLOAD_DIR = "uploads/";
        
        private FileUpload() {}
    }
    
    /**
     * Validation constants
     */
    public static final class Validation {
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MIN_DESCRIPTION_LENGTH = 10;
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
        public static final double MIN_PRICE = 0.01;
        public static final double MAX_PRICE = 999999.99;
        public static final int MIN_QUANTITY = 1;
        public static final int MAX_QUANTITY = 20;
        
        private Validation() {}
    }
    
    /**
     * Distance calculation constants
     */
    public static final class Distance {
        public static final double EARTH_RADIUS_KM = 6371.0088;
        public static final double DEFAULT_MAX_DISTANCE_KM = 50.0;
        
        private Distance() {}
    }
    
    /**
     * Time constants
     */
    public static final class Time {
        public static final int DEFAULT_PICKUP_MINUTES = 30;
        public static final int ORDER_EXPIRY_MINUTES = 15;
        public static final int MAX_PICKUP_MINUTES = 120;
        
        private Time() {}
    }
}
