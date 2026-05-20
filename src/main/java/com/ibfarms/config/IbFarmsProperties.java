package com.ibfarms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ibfarms")
public class IbFarmsProperties {

    /** Email that receives new registration approval requests. */
    private String adminEmail = "musamakhan201@gmail.com";

    /** Public base URL used in approval links (no trailing slash). */
    private String baseUrl = "http://localhost:8080";

    /** From address for outgoing mail (defaults to SMTP username). */
    private String mailFrom;
}
