package com.ibfarms.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts Render/Heroku-style {@code postgres://} URLs to Spring JDBC settings.
 */
public final class DatabaseUrlConverter {

    private DatabaseUrlConverter() {
    }

    public static Map<String, Object> toSpringDatasourceProperties(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return Map.of();
        }

        String normalized = databaseUrl.trim();
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        } else if (!normalized.startsWith("postgresql://")) {
            return Map.of();
        }

        URI uri = URI.create(normalized);
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (path.isEmpty()) {
            path = "/ibfarms";
        }

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(':')
                .append(port)
                .append(path);

        String query = uri.getQuery();
        if (query != null && !query.isBlank()) {
            jdbc.append('?').append(query);
        } else if (host != null && host.contains("render.com")) {
            jdbc.append("?sslmode=require");
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", jdbc.toString());

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            properties.put("spring.datasource.username", decode(parts[0]));
            if (parts.length > 1) {
                properties.put("spring.datasource.password", decode(parts[1]));
            }
        }

        return properties;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
