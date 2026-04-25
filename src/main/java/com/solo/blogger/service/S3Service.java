package com.solo.blogger.service;

import com.solo.blogger.dto.apiRequest.UploadUrlRequest;
import com.solo.blogger.dto.apiResponse.UploadUrlResponse;
import com.solo.blogger.dto.responseFactory.SuccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.folder:posts/}")
    private String folder;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final S3Client s3Client;
    private final S3Presigner presigner;

    public S3Service(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    // -----------------------------
    // ✅ EXISTING POST FLOW (UNCHANGED LOGIC)
    // -----------------------------
    public String storeFile(MultipartFile file, String postTitle) {
        validateFile(file);

        String extension = getExtensionFromContentType(file.getContentType());
        String key = generateS3Key(postTitle, extension);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );

            return key;

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    // -----------------------------
    // ✅ PRE-SIGNED URL (PROFILE PIC)
    // -----------------------------
    public Map<String, String> generatePresignedUrl(String userId, String fileName) {

        String key = String.format("users/%s/profile/%d_%s",
                userId,
                System.currentTimeMillis(),
                fileName);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/jpeg")
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(putRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        String uploadUrl = presignedRequest.url().toString();

        String fileUrl = String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                key
        );

        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl);
        response.put("fileUrl", fileUrl);

        return response;
    }

    // -----------------------------
    // DELETE
    // -----------------------------
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    // -----------------------------
    // UTIL METHODS (UNCHANGED)
    // -----------------------------
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File too large");
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Invalid file type");
        }
    }

    private String generateS3Key(String title, String ext) {
        String clean = title.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return folder + "post-" + clean + "-" + System.currentTimeMillis() + "." + ext;
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            default -> "jpg";
        };
    }

    public String getFileUrl(String key) {
        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                key
        );
    }

    public UploadUrlResponse generateProfileUploadUrl(UploadUrlRequest request) {

        // 🔒 Validate file type
        if (!request.getContentType().startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        String key = "soloblogger/" + UUID.randomUUID() + "-" + request.getFileName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
//                .contentType(request.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(presignRequest);

        return UploadUrlResponse.builder().uploadUrl(presignedRequest.url().toString()).key(key).build();

    }


}
