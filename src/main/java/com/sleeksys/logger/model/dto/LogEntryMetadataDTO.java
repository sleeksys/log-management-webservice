package com.sleeksys.logger.model.dto;

import lombok.Data;

@Data
public class LogEntryMetadataDTO {
    private String os;
    private String language;
    private String browser;
    private String userAgent;
}