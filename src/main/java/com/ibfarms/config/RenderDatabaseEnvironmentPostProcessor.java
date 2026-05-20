package com.ibfarms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps Render's {@code DATABASE_URL} to Spring datasource properties when JDBC URL is not set.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_URL"))) {
            return;
        }

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl)) {
            return;
        }

        Map<String, Object> converted = new LinkedHashMap<>(DatabaseUrlConverter.toSpringDatasourceProperties(databaseUrl));
        if (converted.isEmpty()) {
            return;
        }

        if (StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_USERNAME"))) {
            converted.remove("spring.datasource.username");
        }
        if (StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_PASSWORD"))) {
            converted.remove("spring.datasource.password");
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, converted));
    }
}
