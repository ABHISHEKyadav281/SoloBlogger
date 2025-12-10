package com.solo.blogger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/posts}")
    private String uploadDir;

    private Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            // Create directories if they don't exist
            Files.createDirectories(this.fileStorageLocation);

            System.out.println("✅ File upload directory initialized at: " + this.fileStorageLocation.toString());
        } catch (Exception ex) {
            System.err.println("❌ Failed to create upload directory: " + uploadDir);
            ex.printStackTrace();
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored: " + uploadDir, ex);
        }
    }

    /**
     * Store file and return the stored filename
     */
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Get original filename and clean it
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Generate unique filename with original extension
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Check if filename contains invalid characters
            if (newFilename.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + newFilename);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ File stored successfully: " + newFilename);
            return newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + newFilename + ". Please try again!", ex);
        }
    }

    /**
     * Load file as Resource for serving
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    /**
     * Delete file from storage
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            System.out.println("✅ File deleted: " + fileName);
        } catch (IOException ex) {
            System.err.println("❌ Could not delete file: " + fileName);
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }

    /**
     * Validate file before storing
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new RuntimeException("Only image files (JPG, JPEG, PNG, GIF, WEBP) are allowed");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Get the base URL for file access
     */
    public String getFileUrl(String fileName) {
        return "/api/files/" + fileName;
    }
}