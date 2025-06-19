package com.sleeksys.logger.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Document(collection = "log_authentication")
@Data
public class AuthEntry {
    @Id
    private String id;
    private String createdAt;
    private Long timestamp;
    private Long expiresAt;

    @NotNull
    private String trackingId;
    private String subscriberId;
    private String accessToken;
}
