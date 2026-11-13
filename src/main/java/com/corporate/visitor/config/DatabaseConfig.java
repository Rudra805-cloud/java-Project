package com.corporate.visitor.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record DatabaseConfig(String url, String user, String password) {
    public static DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read database configuration.", exception);
        }

        String url = value(properties, "db.url", "DB_URL",
                "jdbc:mysql://localhost:3306/corporate_visitors?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true");
        String user = value(properties, "db.user", "DB_USER", "root");
        String password = value(properties, "db.password", "DB_PASSWORD", "");
        return new DatabaseConfig(url, user, password);
    }

    private static String value(Properties properties, String property, String environment, String fallback) {
        String configured = properties.getProperty(property);
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank() ? fallback : environmentValue;
    }
}
