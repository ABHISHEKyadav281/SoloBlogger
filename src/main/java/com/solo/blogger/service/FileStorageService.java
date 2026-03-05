package com.solo.blogger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/posts}")
    private String uploadDir;

    private Path fileStorageLocation;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(this.fileStorageLocation);

        } catch (Exception ex) {
            System.err.println("❌ Failed to create upload directory: " + uploadDir);
            ex.printStackTrace();
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored: " + uploadDir, ex);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFile(file);

        String newFilename = UUID.randomUUID().toString() + ".jpg";

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);

            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null) {
                throw new RuntimeException("Could not read image file. File may be corrupted.");
            }

            BufferedImage jpegImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            jpegImage.createGraphics().drawImage(bufferedImage, 0, 0, java.awt.Color.WHITE, null);

            ImageIO.write(jpegImage, "jpg", targetLocation.toFile());

            System.out.println("✅ File converted and stored as JPEG: " + newFilename);
            return newFilename;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + newFilename + ". Please try again!", ex);
        }
    }

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

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public String getFileUrl(String fileName) {
        return "/api/files/" + fileName;
    }
}