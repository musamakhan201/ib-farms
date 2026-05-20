package com.ibfarms.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps {@code DATABASE_URL} / {@code SPRING_DATASOURCE_*} env vars to Spring datasource properties.
 * Runs after config files load so empty property placeholders do not block Neon/Render URLs.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = new LinkedHashMap<>();

        String jdbcUrl = firstText(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("spring.datasource.url"));

        if (!StringUtils.hasText(jdbcUrl)) {
            String databaseUrl = environment.getProperty("DATABASE_URL");
            if (StringUtils.hasText(databaseUrl)) {
                properties.putAll(DatabaseUrlConverter.toSpringDatasourceProperties(databaseUrl));
            }
        } else if (jdbcUrl.startsWith("jdbc:")) {
            properties.put("spring.datasource.url", jdbcUrl);
        } else {
            properties.putAll(DatabaseUrlConverter.toSpringDatasourceProperties(jdbcUrl));
        }

        putIfAbsent(properties, "spring.datasource.username", environment.getProperty("SPRING_DATASOURCE_USERNAME"));
        putIfAbsent(properties, "spring.datasource.password", environment.getProperty("SPRING_DATASOURCE_PASSWORD"));

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static void putIfAbsent(Map<String, Object> target, String key, String value) {
        if (!target.containsKey(key) && StringUtils.hasText(value)) {
            target.put(key, value);
        }
    }

    private static String firstText(String... candidates) {
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                return candidate.trim();
            }
        }
        return null;
    }
}
