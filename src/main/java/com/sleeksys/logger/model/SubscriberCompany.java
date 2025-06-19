package com.sleeksys.logger.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Document(collection = "log_subscriber_companies")
@Data
public class SubscriberCompany {
    @Id
    private String id;
    private String createdAt;

    @NotNull
    private String trackingId;

    @Pattern(regexp = "^[a-zA-Z0-9 ]{1,20}$", message = "Company name must be alphanumeric and up to 20 characters long")
    private String companyName;

    private String allowedOrigins;
}
