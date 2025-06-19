package com.sleeksys.logger.model.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRequestDTO {
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Invalid email address format")
    private String email;

    @Pattern(regexp = "^[a-zA-Z0-9]{1,32}$",
            message = "Password hash must be alphanumeric and contains up to 32 characters long")
    private String password;
}
