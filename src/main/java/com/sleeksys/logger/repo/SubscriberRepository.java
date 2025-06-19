package com.sleeksys.logger.repo;

import com.sleeksys.logger.model.Subscriber;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends MongoRepository<Subscriber, String> {
    @Query("{'companyId': ?0}")
    List<Subscriber> findByCompanyId(String companyId);

    @Query("{'emailAddress': ?0}")
    Optional<Subscriber> findByEmailAddress(String emailAddress);
}
