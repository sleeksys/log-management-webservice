package com.sleeksys.logger.service;

import com.sleeksys.logger.model.SubscriberCompany;
import com.sleeksys.logger.model.dto.AuthResponseDTO;
import com.sleeksys.logger.repo.SubscriberCompanyRepository;
import com.sleeksys.logger.utils.DateUtils;
import com.sleeksys.logger.utils.ServiceUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriberCompanyService {

    private final SubscriberCompanyRepository repository;

    public Optional<SubscriberCompany> findById(String id) {
        return this.repository.findById(id);
    }

    public Optional<SubscriberCompany> findByTrackingId(String trackingId) {
        return this.repository.findByTrackingId(trackingId);
    }

    public SubscriberCompany create(SubscriberCompany company) {
        company.setCreatedAt(DateUtils.formatDate());
        company.setTrackingId(ServiceUtils.generateTrackingId());
        company.setAllowedOrigins(cleanAllowedOrigin(company.getAllowedOrigins()));

        return this.repository.insert(company);
    }

    public SubscriberCompany update(AuthResponseDTO authResponse,
                                    SubscriberCompany company) throws IllegalArgumentException, SecurityException {
        Optional<SubscriberCompany> existingCompanyOpt = this.repository.findById(company.getId());
        if (existingCompanyOpt.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (!existingCompanyOpt.get().getTrackingId().equals(authResponse.getTrackingId())) {
            throw new SecurityException();
        }

        // don't allow changing the tracking ID or creation date
        SubscriberCompany tmp = existingCompanyOpt.get();
        company.setCreatedAt(tmp.getCreatedAt());
        company.setTrackingId(tmp.getTrackingId());

        return this.repository.save(company);
    }

    /** Removes http(s):// and www. from the origins. */
    private String cleanAllowedOrigin(String allowedOrigins) {
        return allowedOrigins.replaceAll("^(https?://)?(www\\.)?", "");
    }
}
