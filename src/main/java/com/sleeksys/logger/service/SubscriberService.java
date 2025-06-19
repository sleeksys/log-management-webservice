package com.sleeksys.logger.service;

import com.sleeksys.logger.model.Subscriber;
import com.sleeksys.logger.model.SubscriberCompany;
import com.sleeksys.logger.repo.SubscriberRepository;
import com.sleeksys.logger.utils.DateUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriberService {

    private final SubscriberRepository repository;
    private final SubscriberCompanyService companyService;

    public List<Subscriber> findByCompany(String trackingId) {
        Optional<SubscriberCompany> optional = this.companyService.findByTrackingId(trackingId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Company not found for trackingId: " + trackingId);
        }

        String companyId = optional.get().getId();
        return this.repository.findByCompanyId(companyId)
                .stream()
                .peek(subscriber -> {
                    subscriber.setPasswordHash(null); // Remove password hash for security
                })
                .toList();
    }

    public Optional<Subscriber> findById(String id) {
        return this.repository.findById(id)
                .flatMap(subscriber -> {
                    subscriber.setPasswordHash(null); // Remove password hash for security
                    return Optional.of(subscriber);
                });
    }

    public Optional<Subscriber> findByEmailAddress(String emailAddress, String password) {
        return this.repository.findByEmailAddress(emailAddress)
                .filter(subscriber -> Objects.equals(subscriber.getPasswordHash(), password))
                .flatMap(subscriber -> {
                    subscriber.setPasswordHash(null); // Remove password hash for security
                    return Optional.of(subscriber);
                });
    }

    public Subscriber create(String trackingId, Subscriber subscriber) throws IllegalArgumentException {
        Optional<SubscriberCompany> optional = this.companyService.findByTrackingId(trackingId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Company not found for trackingId: " + trackingId);
        }

        subscriber.setCreatedAt(DateUtils.formatDate());
        subscriber.setCompanyId(optional.get().getId());

        // use the encryptPassword method to hash the password
        // subscriber.setPasswordHash(encryptPassword(subscriber.getPasswordHash()));

        return this.repository.insert(subscriber);
    }

    public String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
