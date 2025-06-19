package com.sleeksys.logger.model;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Document(collection = "log_subscribers")
@Data
public class Subscriber {
    @Id
    private String id;
    private String createdAt;
    private String companyId;

    @Pattern(regexp = "^[a-zA-Z]{1,15}$", message = "First name must be alphabetic and up to 15 characters long")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Z]{1,15}$", message = "Last name must be alphabetic and up to 15 characters long")
    private String lastName;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email address format")
    private String emailAddress;

    // max 32 characters
    @Pattern(regexp = "^[a-zA-Z0-9]{1,32}$", message = "Password hash must be alphanumeric and up to 32 characters long")
    private String passwordHash;
}
