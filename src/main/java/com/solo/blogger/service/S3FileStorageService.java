package com.solo.blogger.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class S3FileStorageService {

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.folder:posts/}")
    private String folder;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();

            System.out.println("S3 client initialized. Bucket: " + bucketName + " | Region: " + region);
        } catch (Exception ex) {
            System.err.println("Failed to initialize S3 client: " + ex.getMessage());
            throw new RuntimeException("Could not initialize AWS S3 client", ex);
        }
    }

    public String storeFile(MultipartFile file, String postTitle) {
        validateFile(file);

        // Get original file extension from content type
        String extension = getExtensionFromContentType(file.getContentType());
        String s3Key = generateS3Key(postTitle, extension);

        try {
            // ── JPEG conversion commented out ──────────────────────────
            // byte[] imageBytes = convertToJpeg(file);
            // String contentType = "image/jpeg";
            // ───────────────────────────────────────────────────────────

            // Upload original file as-is
            byte[] imageBytes = file.getBytes();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : "application/octet-stream";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(imageBytes.length);

            PutObjectRequest putRequest = new PutObjectRequest(
                    bucketName,
                    s3Key,
                    new ByteArrayInputStream(imageBytes),
                    metadata
            );

            s3Client.putObject(putRequest);
            System.out.println("Image uploaded to S3: " + s3Key);

            return s3Key;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to upload image to S3: " + ex.getMessage(), ex);
        }
    }

    public String getFileUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    public void deleteFile(String s3Key) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, s3Key));
            System.out.println("File deleted from S3: " + s3Key);
        } catch (Exception ex) {
            System.err.println("Could not delete file from S3: " + s3Key);
            throw new RuntimeException("Could not delete file from S3: " + s3Key, ex);
        }
    }

    // ── JPEG conversion logic (commented out) ─────────────────────────
    //
    // private byte[] convertToJpeg(MultipartFile file) throws IOException {
    //     byte[] fileBytes = file.getBytes();
    //
    //     BufferedImage originalImage = null;
    //     try {
    //         originalImage = ImageIO.read(new ByteArrayInputStream(fileBytes));
    //     } catch (Exception e) {
    //         System.err.println("ImageIO failed: " + e.getMessage());
    //     }
    //
    //     BufferedImage jpegImage = new BufferedImage(
    //             originalImage.getWidth(),
    //             originalImage.getHeight(),
    //             BufferedImage.TYPE_INT_RGB
    //     );
    //     Graphics2D g2d = jpegImage.createGraphics();
    //     g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    //     g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    //     g2d.setColor(Color.WHITE);
    //     g2d.fillRect(0, 0, jpegImage.getWidth(), jpegImage.getHeight());
    //     g2d.drawImage(originalImage, 0, 0, null);
    //     g2d.dispose();
    //
    //     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //     boolean written = ImageIO.write(jpegImage, "jpeg", outputStream);
    //     if (!written) {
    //         throw new RuntimeException("Failed to write image as JPEG.");
    //     }
    //
    //     return outputStream.toByteArray();
    // }
    //
    // ──────────────────────────────────────────────────────────────────

    private static final int TITLE_WORD_LIMIT = 3;

    // Updated to accept extension instead of hardcoding .jpeg
    private String generateS3Key(String postTitle, String extension) {
        String[] words = postTitle.trim().split("\\s+");
        int wordCount = Math.min(words.length, TITLE_WORD_LIMIT);
        StringBuilder titleBuilder = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) titleBuilder.append("-");
            titleBuilder.append(words[i].toLowerCase().replaceAll("[^a-z0-9]", ""));
        }

        String shortTitle = titleBuilder.toString();
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // Uses original extension instead of hardcoded .jpeg
        String filename = "post-" + shortTitle + "-" + timestamp + "." + extension;
        return folder + filename;
    }

    // Maps content type to file extension
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return "bin";
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            case "image/svg+xml" -> "svg";
            default -> "jpg";
        };
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file. Please upload a valid image.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                    String.format("File size %.2f MB exceeds the maximum allowed size of 10 MB.",
                            file.getSize() / (1024.0 * 1024.0))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Invalid file type: '" + contentType + "'. Only image files are allowed.");
        }
    }
}