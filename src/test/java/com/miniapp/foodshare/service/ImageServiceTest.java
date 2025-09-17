//package com.miniapp.foodshare.service;
//
//import com.cloudinary.Cloudinary;
//import com.miniapp.foodshare.dto.ImageUploadRequest;
//import com.miniapp.foodshare.dto.ImageUploadResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyMap;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ImageServiceTest {
//
//    @Mock
//    private Cloudinary cloudinary;
//
//    @Mock
//    private com.cloudinary.Uploader uploader;
//
//    @InjectMocks
//    private ImageService imageService;
//
//    @BeforeEach
//    void setUp() {
//        when(cloudinary.uploader()).thenReturn(uploader);
//    }
//
//    @Test
//    void uploadImage_Success() throws IOException {
//        // Arrange
//        MultipartFile file = new MockMultipartFile(
//            "file",
//            "test.jpg",
//            "image/jpeg",
//            "test image content".getBytes()
//        );
//
//        ImageUploadRequest request = new ImageUploadRequest();
//        request.setFile(file);
//        request.setFolder("test-folder");
//
//        Map<String, Object> mockResult = new HashMap<>();
//        mockResult.put("public_id", "test-folder/test");
//        mockResult.put("secure_url", "https://res.cloudinary.com/test/image/upload/v123/test-folder/test.jpg");
//        mockResult.put("original_filename", "test.jpg");
//        mockResult.put("format", "jpg");
//        mockResult.put("bytes", 1024L);
//        mockResult.put("width", 1920);
//        mockResult.put("height", 1080);
//        mockResult.put("resource_type", "image");
//
//        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);
//
//        // Act
//        ImageUploadResponse response = imageService.uploadImage(request);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals("test-folder/test", response.getPublicId());
//        assertEquals("https://res.cloudinary.com/test/image/upload/v123/test-folder/test.jpg", response.getSecureUrl());
//        assertEquals("test.jpg", response.getOriginalFilename());
//        assertEquals("jpg", response.getFormat());
//        assertEquals(1024L, response.getBytes());
//        assertEquals(1920, response.getWidth());
//        assertEquals(1080, response.getHeight());
//        assertEquals("image", response.getResourceType());
//    }
//
//    @Test
//    void uploadImage_EmptyFile_ThrowsException() {
//        // Arrange
//        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
//        ImageUploadRequest request = new ImageUploadRequest();
//        request.setFile(file);
//
//        // Act & Assert
//        assertThrows(IllegalArgumentException.class, () -> imageService.uploadImage(request));
//    }
//
//    @Test
//    void uploadImage_InvalidFileType_ThrowsException() {
//        // Arrange
//        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
//        ImageUploadRequest request = new ImageUploadRequest();
//        request.setFile(file);
//
//        // Act & Assert
//        assertThrows(IllegalArgumentException.class, () -> imageService.uploadImage(request));
//    }
//
//    @Test
//    void uploadImage_FileTooLarge_ThrowsException() {
//        // Arrange
//        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
//        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", largeContent);
//        ImageUploadRequest request = new ImageUploadRequest();
//        request.setFile(file);
//
//        // Act & Assert
//        assertThrows(IllegalArgumentException.class, () -> imageService.uploadImage(request));
//    }
//}
