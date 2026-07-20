package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    // Prod: raw JSON or base64-encoded JSON, from FIREBASE_SERVICE_ACCOUNT_JSON.
    @Value("${firebase.service.account.json:}")
    private String serviceAccountJson;

    // Local dev fallback: a classpath file each developer keeps gitignored.
    @Value("${firebase.service.account:}")
    private Resource serviceAccountResource;

    @PostConstruct
    public void initialize() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized, skipping.");
            return;
        }

        GoogleCredentials credentials;

        if (StringUtils.hasText(serviceAccountJson)) {
            log.info("Initializing Firebase from FIREBASE_SERVICE_ACCOUNT_JSON env var.");
            credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(decode(serviceAccountJson)));
        } else if (serviceAccountResource != null && serviceAccountResource.exists()) {
            log.info("Initializing Firebase from local classpath resource: {}",
                    serviceAccountResource.getFilename());
            credentials = GoogleCredentials.fromStream(serviceAccountResource.getInputStream());
        } else {
            throw new IllegalStateException(
                    "No Firebase credentials found. Set FIREBASE_SERVICE_ACCOUNT_JSON " +
                    "(prod) or firebase.service.account pointing to a valid classpath file (local dev).");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase initialized successfully.");
    }

    private byte[] decode(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("{")) {
            return trimmed.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return Base64.getDecoder().decode(trimmed);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "FIREBASE_SERVICE_ACCOUNT_JSON is set but is neither valid JSON " +
                    "nor valid base64. Check the env var value on your host.", e);
        }
    }
}