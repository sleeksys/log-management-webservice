package com.sleeksys.logger.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LogEntryDTO {
    @NotNull
    private String credentials;
    private String origin;
    private String level = "INFO";
    @NotNull
    private String name;
    @NotNull
    private String message;
    private String url;
    private LogEntryMetadataDTO metadata;

    public LogEntryMetadataDTO getMetadata() {
        if (this.metadata == null) {
            this.metadata = new LogEntryMetadataDTO();
        }
        return this.metadata;
    }
}