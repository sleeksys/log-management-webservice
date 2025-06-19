package com.sleeksys.logger.service;

import com.sleeksys.logger.model.SubscriberCompany;
import com.sleeksys.logger.model.dto.AuthRequestDTO;
import com.sleeksys.logger.model.dto.AuthResponseDTO;
import com.sleeksys.logger.model.AuthEntry;
import com.sleeksys.logger.model.Subscriber;
import com.sleeksys.logger.repo.AuthRepository;
import com.sleeksys.logger.utils.DateUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthRepository repository;
    private final SubscriberService subscriberService;
    private final SubscriberCompanyService companyService;

    public AuthResponseDTO login(AuthRequestDTO request) {
        Optional<Subscriber> optional = subscriberService.findByEmailAddress(request.getEmail(), request.getPassword());
        // subscriber for found for given credentials
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Invalid login credentials");
        }

        AuthEntry entry = create(optional.get());
        return mapEntryToResponseDto(entry);
    }

    public AuthResponseDTO check(String accessToken) {
        Optional<AuthEntry> optional = this.findByAccessToken(accessToken);
        // no auth entry found for the given access token
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Invalid access token");
        }

        AuthEntry entry = optional.get();
        // check if the access token has expired
        if (entry.getExpiresAt() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Access token has expired");
        }

        // create a new access token if entry older than 1 hour ago
        long nowOneHourAgo = Calendar.getInstance().getTimeInMillis() - 3600000; // 1 hour in milliseconds
        if (nowOneHourAgo > entry.getTimestamp()) {
            return update(entry.getSubscriberId());
        }
        // otherwise, just refresh the existing entry
        return refresh(entry);
    }

    public Optional<AuthEntry> findByAccessToken(String accessToken) {
        return this.repository.findByAccessToken(accessToken);
    }

    public AuthEntry create(Subscriber subscriber) throws IllegalArgumentException {
        Optional<SubscriberCompany> companyOpt = this.companyService.findById(subscriber.getCompanyId());
        if (companyOpt.isEmpty()) {
            throw new IllegalArgumentException("Company not found for subscriber ID: " + subscriber.getId());
        }

        Calendar calendar = Calendar.getInstance();

        AuthEntry entry = new AuthEntry();
        entry.setCreatedAt(DateUtils.formatDate());
        entry.setTimestamp(calendar.getTimeInMillis());
        entry.setExpiresAt(calendar.getTimeInMillis() + 1800000); // 30 minutes expiration
        entry.setTrackingId(companyOpt.get().getTrackingId());
        entry.setSubscriberId(subscriber.getId());
        entry.setAccessToken(generateAccessToken());

        return this.repository.insert(entry);
    }

    public AuthResponseDTO update(String subscriberId) {
        Optional<Subscriber> optional = this.subscriberService.findById(subscriberId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("No active session found for subscriber ID: " + subscriberId);
        }

        AuthEntry entry = create(optional.get());
        return mapEntryToResponseDto(entry);
    }

    public AuthResponseDTO refresh(AuthEntry entry) {
        // extend the expiration time by 5 minutes
        entry.setExpiresAt(entry.getExpiresAt() + 300000); // 5 minutes extension
        repository.save(entry);

        return mapEntryToResponseDto(entry);
    }

    /** Generates random string in base64 encoding with 32 chars. */
    private String generateAccessToken() {
        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();

        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private AuthResponseDTO mapEntryToResponseDto(AuthEntry entry) {
        long now = Calendar.getInstance().getTimeInMillis();
        long diffInSeconds = (entry.getExpiresAt() - now) / 1000;

        AuthResponseDTO response = new AuthResponseDTO();
        response.setExpiresIn(diffInSeconds);
        response.setTrackingId(entry.getTrackingId());
        response.setSubscriberId(entry.getSubscriberId());
        response.setAccessToken(entry.getAccessToken());

        return response;
    }
}
