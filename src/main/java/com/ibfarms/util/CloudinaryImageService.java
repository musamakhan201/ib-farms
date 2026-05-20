package com.ibfarms.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.ibfarms.config.CloudinaryProperties;
import com.ibfarms.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    /**
     * Uploads an image and returns the Cloudinary public ID (stored on {@link com.ibfarms.entity.Animal#pictureFilename}).
     */
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (original.contains("..")) {
            throw new IOException("Invalid file name");
        }
        String ext = getExtension(original);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IOException("Only image files (jpg, png, gif, webp) are allowed");
        }

        String publicId = properties.getFolder() + "/" + UUID.randomUUID();

        @SuppressWarnings("unchecked")
        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", false);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            Object id = result.get("public_id");
            if (id == null) {
                throw new BusinessException("Cloudinary upload failed: missing public_id");
            }
            return id.toString();
        } catch (Exception ex) {
            throw new BusinessException("Failed to upload image to Cloudinary: " + ex.getMessage());
        }
    }

    public void deleteIfExists(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        if (isLegacyLocalReference(publicId)) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ex) {
            throw new BusinessException("Failed to delete image from Cloudinary: " + ex.getMessage());
        }
    }

    public String buildUrl(String publicId) {
        return buildUrl(publicId, detailTransformation());
    }

    public String buildThumbnailUrl(String publicId) {
        return buildUrl(publicId, thumbTransformation());
    }

    public String buildUrl(String publicId, Transformation<?> transformation) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        if (isLegacyLocalReference(publicId)) {
            return "/uploads/" + publicId;
        }
        return cloudinary.url()
                .secure(true)
                .publicId(publicId)
                .transformation(transformation)
                .generate();
    }

    private Transformation<?> detailTransformation() {
        return new Transformation<>()
                .width(400)
                .height(400)
                .crop("limit")
                .quality("auto")
                .fetchFormat("auto");
    }

    private Transformation<?> thumbTransformation() {
        return new Transformation<>()
                .width(96)
                .height(96)
                .crop("fill")
                .quality("auto")
                .fetchFormat("auto");
    }

    private boolean isLegacyLocalReference(String value) {
        return !value.contains("/") && !value.startsWith("http");
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return null;
        }
        return filename.substring(i + 1);
    }
}
