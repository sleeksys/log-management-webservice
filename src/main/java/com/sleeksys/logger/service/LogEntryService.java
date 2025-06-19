package com.sleeksys.logger.service;

import com.sleeksys.logger.model.SubscriberCompany;
import com.sleeksys.logger.model.dto.AuthResponseDTO;
import com.sleeksys.logger.model.dto.LogEntryDTO;
import com.sleeksys.logger.model.LogEntry;
import com.sleeksys.logger.repo.LogEntryRepository;
import com.sleeksys.logger.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class LogEntryService {

    private final LogEntryRepository repository;
    private final SubscriberCompanyService companyService;

    public List<LogEntry> findAll(String trackingId) {
        this.companyService.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for tracking ID: " + trackingId));

        List<LogEntry> list = this.repository.findByTrackingId(trackingId);
        return sortByDate(list);
    }

    public List<LogEntry> findByPeriod(String trackingId, Integer year,
                                       @Nullable Integer month) {
        this.companyService.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for tracking ID: " + trackingId));

        List<LogEntry> list = (month != null)
                ? this.repository.findByMonth(trackingId, year, month)
                : this.repository.findByYear(trackingId, year);
        return sortByDate(list);
    }

    public List<LogEntry> findByPeriodAndPagination(String trackingId,
                                                    @Nullable Integer year,
                                                    @Nullable Integer month,
                                                    @Nullable String dateFrom,
                                                    @Nullable String dateTo,
                                                    @Nullable Integer currentPage,
                                                    @Nullable Integer itemsPerPage) {
        this.companyService.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for tracking ID: " + trackingId));

        List<LogEntry> entries = (year != null) ? this.findByPeriod(trackingId, year, month) : findAll(trackingId);
        entries = sortByDate(entries);

        // filter by date range if specified
        if (dateFrom != null || dateTo != null) {
            entries = entries.stream()
                    .filter(entry -> {
                        String entryDate = entry.getDate();
                        return (dateFrom == null || entryDate.compareTo(dateFrom) >= 0) &&
                               (dateTo == null || entryDate.compareTo(dateTo) <= 0);
                    })
                    .toList();
        }


        // no pagination
        if (currentPage == null || itemsPerPage == null) {
            return entries; // return all entries if pagination is not specified
        }

        // use pagination
        int startIndex = (currentPage - 1) * itemsPerPage;
        if (startIndex >= entries.size()) {
            return List.of(); // return empty list if start index is out of bounds
        }
        int endIndex = Math.min(startIndex + itemsPerPage, entries.size());
        return entries.subList(startIndex, endIndex);
    }

    public List<LogEntry> findByKey(String trackingId, String key) {
        this.companyService.findByTrackingId(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found for tracking ID: " + trackingId));

        List<LogEntry> list = this.repository.findByNameOrMessageContaining(trackingId, key);
        return sortByDate(list);
    }

    public Integer countDuplicateById(String trackingId, String logEntryId) {
        Optional<LogEntry> optional = this.repository.findById(logEntryId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Log entry not found for ID: " + logEntryId);
        }

        LogEntry logEntry = optional.get();
        List<LogEntry> entries = this.repository.findByNameAndMessage(trackingId, logEntry.getName(), logEntry.getMessage());
        return entries.size() - 1;
    }

    public LogEntry create(LogEntryDTO dtoEntry) {
        String trackingId = dtoEntry.getCredentials();

        Optional<SubscriberCompany> subscriberOpt = this.companyService.findByTrackingId(trackingId);
        if (subscriberOpt.isEmpty()) {
            throw new IllegalArgumentException("Company not found for tracking ID: " + trackingId);
        }

        // TODO: validate allowed origins if necessary

        Calendar today = Calendar.getInstance();

        LogEntry entry = new LogEntry();

        // track current date
        entry.setDate(DateUtils.formatDate());
        entry.setYear(today.get(Calendar.YEAR));
        entry.setMonth(today.get(Calendar.MONTH) + 1); // Calendar.MONTH is zero-based

        // set entry details
        entry.setLevel(mapLoggingLevel(dtoEntry.getLevel()));
        entry.setTrackingId(trackingId);
        entry.setOrigin(dtoEntry.getOrigin());
        entry.setName(dtoEntry.getName());
        entry.setMessage(dtoEntry.getMessage());
        entry.setUrl(dtoEntry.getUrl());

        // set entry metadata
        entry.setMetadataClientOs(dtoEntry.getMetadata().getOs());
        entry.setMetadataClientLanguage(dtoEntry.getMetadata().getLanguage());
        entry.setMetadataClientBrowser(dtoEntry.getMetadata().getBrowser());
        entry.setMetadataClientUserAgent(dtoEntry.getMetadata().getUserAgent());

        return this.repository.insert(entry);
    }

    public boolean pin(AuthResponseDTO authResponseDTO,
                       String id) throws SecurityException, IllegalArgumentException {
        Optional<LogEntry> optional = this.repository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Log entry not found for ID: " + id);
        }
        if (!optional.get().getTrackingId().equals(authResponseDTO.getTrackingId())) {
            throw new SecurityException("Access denied: You do not have permission to pin this log entry.");
        }

        boolean isAlreadyPinned = optional.get().isPinned();

        LogEntry logEntry = optional.get();
        logEntry.setPinned(!isAlreadyPinned); // toggle pin status
        logEntry.setPinnedAt(!isAlreadyPinned ? DateUtils.formatDate() : null);
        logEntry.setPinnedBy(!isAlreadyPinned ? authResponseDTO.getSubscriberId() : null);

        this.repository.save(logEntry);
        return true;
    }

    public boolean delete(AuthResponseDTO authResponseDTO,
                          String id,
                          boolean deleteSimilarLogs) throws SecurityException, IllegalArgumentException {
        Optional<LogEntry> optional = this.repository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Log entry not found for ID: " + id);
        }
        if (!optional.get().getTrackingId().equals(authResponseDTO.getTrackingId())) {
            throw new SecurityException("Access denied: You do not have permission to delete this log entry.");
        }

        LogEntry logEntry = optional.get();

        // delete only the specified log entry
        if (!deleteSimilarLogs) {
            this.repository.delete(logEntry);
            return true;
        }

        // delete all similar log entries for the same tracking ID
        List<LogEntry> similarEntries = this.repository.findByTrackingId(authResponseDTO.getTrackingId())
                .stream()
                .filter(log -> log.getName().equals(logEntry.getName()) && log.getMessage().equals(logEntry.getMessage()))
                .toList();
        this.repository.deleteAll(similarEntries);
        return true;
    }

    public String getHostname(@PathVariable String ipAddress) {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.getHostName();
        } catch (Exception e) {
            log.error("Invalid IP address: {}", ipAddress);
        }
        return ipAddress;
    }

    private String mapLoggingLevel(String level) {
        return switch (level.toUpperCase()) {
            case "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL" -> level;
            default -> "INFO";
        };
    }

    private List<LogEntry> sortByDate(List<LogEntry> logEntries) {
        return logEntries
                .stream()
                .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
                .toList();
    }
}
