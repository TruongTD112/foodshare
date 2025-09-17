# Cloudinary Image Upload API Setup

This document explains how to set up and use the Cloudinary image upload API in the FoodShare application.

## Prerequisites

1. Create a Cloudinary account at [https://cloudinary.com](https://cloudinary.com)
2. Get your Cloud Name, API Key, and API Secret from the Cloudinary dashboard

## Configuration

### 1. Update Application Properties

Edit `src/main/resources/application.properties` and replace the placeholder values with your actual Cloudinary credentials:

```properties
# Cloudinary configuration
cloudinary.cloud-name=your_actual_cloud_name
cloudinary.api-key=your_actual_api_key
cloudinary.api-secret=your_actual_api_secret
```

### 2. Build the Application

Run the following command to build the application with the new Cloudinary dependency:

```bash
./gradlew build
```

## API Endpoints

### Upload Image

**POST** `/api/images/upload`

Upload an image file to Cloudinary.

**Parameters:**
- `file` (required): The image file to upload
- `folder` (optional): Folder name in Cloudinary (default: "foodshare")
- `publicId` (optional): Custom public ID for the image
- `transformation` (optional): Cloudinary transformation parameters

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/images/upload" \
  -F "file=@/path/to/image.jpg" \
  -F "folder=products" \
  -F "publicId=product_123"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "publicId": "products/product_123",
    "secureUrl": "https://res.cloudinary.com/your-cloud/image/upload/v1234567890/products/product_123.jpg",
    "originalFilename": "image.jpg",
    "format": "jpg",
    "bytes": 1024000,
    "width": 1920,
    "height": 1080,
    "resourceType": "image"
  }
}
```

### Delete Image

**DELETE** `/api/images/{publicId}`

Delete an image from Cloudinary.

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/images/products/product_123"
```

### Generate Image URL

**GET** `/api/images/url/{publicId}`

Generate a Cloudinary URL for an image with optional transformations.

**Parameters:**
- `publicId` (path): Public ID of the image
- `transformation` (query, optional): Transformation parameters

**Example Request:**
```bash
curl "http://localhost:8080/api/images/url/products/product_123?transformation=w_500,h_500,c_fill"
```

## File Validation

The API includes the following validations:

1. **File Type**: Only image files are allowed
2. **File Size**: Maximum 10MB per file
3. **Required Field**: File parameter is required

## Error Handling

The API returns appropriate error messages for:

- Empty or invalid files
- Files that are too large
- Non-image file types
- Cloudinary upload failures
- Network connectivity issues

## Swagger Documentation

Once the application is running, you can access the Swagger UI at:
`http://localhost:8080/swagger-ui.html`

The image upload endpoints will be documented under the "Image Management" section.

## Testing

Run the tests to verify the image service functionality:

```bash
./gradlew test
```

## Security Considerations

1. **API Credentials**: Keep your Cloudinary API credentials secure and never commit them to version control
2. **File Validation**: The API validates file types and sizes to prevent abuse
3. **Authentication**: Consider adding authentication to the image upload endpoints if needed
4. **Rate Limiting**: Implement rate limiting if you expect high traffic

## Common Transformations

Here are some common Cloudinary transformation parameters:

- `w_500,h_500,c_fill`: Resize to 500x500 with fill crop
- `w_auto,h_auto,q_auto`: Auto-optimize width, height, and quality
- `f_auto`: Auto-format (WebP, AVIF when supported)
- `w_800,h_600,c_limit`: Resize to fit within 800x600 while maintaining aspect ratio
