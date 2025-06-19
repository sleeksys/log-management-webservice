package com.sleeksys.logger.model.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private Long expiresIn;
    private String trackingId;
    private String subscriberId;
    private String accessToken;
}
