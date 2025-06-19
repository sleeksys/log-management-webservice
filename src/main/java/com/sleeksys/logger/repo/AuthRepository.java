package com.sleeksys.logger.repo;

import com.sleeksys.logger.model.AuthEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends MongoRepository<AuthEntry, String> {
    @Query("{'accessToken': ?0}")
    Optional<AuthEntry> findByAccessToken(String accessToken);
}
