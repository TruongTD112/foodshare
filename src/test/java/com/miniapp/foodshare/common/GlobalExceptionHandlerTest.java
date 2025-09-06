package com.miniapp.foodshare.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testMissingParameterExceptionReturnsHttp200() {
        // Create a mock MissingServletRequestParameterException
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("testParam", "String");
        
        // Call the handler
        ResponseEntity<Result<Void>> response = handler.handleMissingParameterException(ex, null);
        
        // Verify HTTP status is 200
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify the response body contains error information
        Result<Void> result = response.getBody();
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.MISSING_REQUIRED_FIELDS.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("testParam"));
    }

    @Test
    void testTypeMismatchExceptionReturnsHttp200() {
        // Create a mock MethodArgumentTypeMismatchException
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "invalidValue", Integer.class, "testParam", null, null);
        
        // Call the handler
        ResponseEntity<Result<Void>> response = handler.handleMethodArgumentTypeMismatchException(ex, null);
        
        // Verify HTTP status is 200
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Verify the response body contains error information
        Result<Void> result = response.getBody();
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.INVALID_REQUEST.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("testParam"));
    }
}
