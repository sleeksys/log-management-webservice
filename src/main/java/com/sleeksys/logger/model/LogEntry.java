package com.sleeksys.logger.model;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Document(collection = "log_entries")
@Data
public class LogEntry {
    @Id
    private String id;
    private String date;
    private Integer year;
    private Integer month;

    // TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    @Pattern(regexp = "^(TRACE|DEBUG|INFO|WARN|ERROR|FATAL)$", message = "Level must be one of: TRACE, DEBUG, INFO, WARN, ERROR, FATAL")
    private String level = "INFO";
    private String trackingId;
    private String origin;
    private String name;
    private String message;
    private String url;

    private boolean pinned;
    private String pinnedAt;
    private String pinnedBy;

    private String metadataClientOs;
    private String metadataClientLanguage;
    private String metadataClientBrowser;
    private String metadataClientUserAgent;

    private String fileLineNumber;
    private String fileColumnNumber;
}