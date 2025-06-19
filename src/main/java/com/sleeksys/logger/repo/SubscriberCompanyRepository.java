package com.sleeksys.logger.repo;

import com.sleeksys.logger.model.SubscriberCompany;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberCompanyRepository extends MongoRepository<SubscriberCompany, String> {
    @Query("{'trackingId': ?0}")
    Optional<SubscriberCompany> findByTrackingId(String trackingId);
}
