package com.ibfarms.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    @NotBlank
    private String cloudName;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiSecret;

  /** Folder prefix for animal images, e.g. ib-farms/animals */
    @NotBlank
    private String folder = "ib-farms/animals";
}
