package com.ibfarms.util;

import org.springframework.stereotype.Component;

/**
 * Thymeleaf helper: {@code ${@imageUrl.url(animal.pictureFilename)}}.
 */
@Component("imageUrl")
public class AnimalImageUrls {

    private final CloudinaryImageService cloudinaryImageService;

    public AnimalImageUrls(CloudinaryImageService cloudinaryImageService) {
        this.cloudinaryImageService = cloudinaryImageService;
    }

    public String url(String publicId) {
        return cloudinaryImageService.buildUrl(publicId);
    }

    public String thumb(String publicId) {
        return cloudinaryImageService.buildThumbnailUrl(publicId);
    }
}
