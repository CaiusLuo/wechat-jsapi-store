package com.example.wechatstore.modules.file.service;

import com.example.wechatstore.common.exception.BizException;
import com.example.wechatstore.config.AppUploadProperties;
import com.example.wechatstore.modules.file.vo.FileUploadVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final AppUploadProperties properties;

    public FileUploadService(AppUploadProperties properties) {
        this.properties = properties;
    }

    public FileUploadVO uploadImage(MultipartFile file) {
        validateFile(file);

        String extension = resolveExtension(file);
        LocalDate today = LocalDate.now();
        String year = String.valueOf(today.getYear());
        String month = "%02d".formatted(today.getMonthValue());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path baseDir = Paths.get(properties.getBaseDir()).toAbsolutePath().normalize();
        Path targetDir = baseDir.resolve(year).resolve(month).normalize();
        Path targetFile = targetDir.resolve(filename).normalize();
        if (!targetFile.startsWith(baseDir)) {
            throw new BizException("invalid upload path");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new BizException("file upload failed");
        }

        String publicUrl = normalizePublicPrefix(properties.getPublicPrefix())
                + "/" + year
                + "/" + month
                + "/" + filename;
        return new FileUploadVO(publicUrl);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("file is required");
        }
        if (file.getSize() > properties.getMaxSize().toBytes()) {
            throw new BizException("file size exceeds limit");
        }
        resolveExtension(file);
    }

    private String resolveExtension(MultipartFile file) {
        String extension = extensionFromFilename(file.getOriginalFilename());
        String normalizedContentType = normalizeContentType(file.getContentType());
        String contentTypeExtension = normalizedContentType == null
                ? null
                : CONTENT_TYPE_EXTENSIONS.get(normalizedContentType);
        if (StringUtils.hasText(extension) && ALLOWED_EXTENSIONS.contains(extension)) {
            if (contentTypeExtension != null && !isSameImageType(extension, contentTypeExtension)) {
                throw new BizException("file type mismatch");
            }
            return extension;
        }
        if (contentTypeExtension != null) {
            return contentTypeExtension;
        }
        throw new BizException("only jpg/jpeg/png/webp files are allowed");
    }

    private String extensionFromFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType.toLowerCase(Locale.ROOT) : null;
    }

    private boolean isSameImageType(String extension, String contentTypeExtension) {
        if ("jpeg".equals(extension)) {
            return "jpg".equals(contentTypeExtension);
        }
        return extension.equals(contentTypeExtension);
    }

    private String normalizePublicPrefix(String publicPrefix) {
        if (!StringUtils.hasText(publicPrefix)) {
            return "/uploads";
        }
        String normalized = publicPrefix.startsWith("/") ? publicPrefix : "/" + publicPrefix;
        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
