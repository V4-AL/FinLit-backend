package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    // Prod: raw JSON or base64-encoded JSON, from FIREBASE_SERVICE_ACCOUNT_JSON.
    @Value("${firebase.service.account.json:}")
    private String serviceAccountJson;

    // Local dev fallback: a classpath file each developer keeps gitignored.
    @Value("${firebase.service.account}")
    private Resource serviceAccountResource;

    @PostConstruct
    public void initialize() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        GoogleCredentials credentials = StringUtils.hasText(serviceAccountJson)
                ? GoogleCredentials.fromStream(new ByteArrayInputStream(decode(serviceAccountJson)))
                : GoogleCredentials.fromStream(serviceAccountResource.getInputStream());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);
    }

    private byte[] decode(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("{")
                ? trimmed.getBytes(StandardCharsets.UTF_8)
                : Base64.getDecoder().decode(trimmed);
    }
}