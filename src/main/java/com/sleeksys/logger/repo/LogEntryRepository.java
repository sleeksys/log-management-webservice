package com.sleeksys.logger.repo;

import com.sleeksys.logger.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    @Query("{'trackingId': ?0}")
    List<LogEntry> findByTrackingId(String trackingId);

    @Query("{'trackingId': ?0, 'year': ?1}")
    List<LogEntry> findByYear(String trackingId, Integer year);

    @Query("{'trackingId': ?0, 'year': ?1, 'month': ?2}")
    List<LogEntry> findByMonth(String trackingId, Integer year, Integer month);

    @Query("{'trackingId': ?0, 'name': ?1, 'message': ?2}")
    List<LogEntry> findByNameAndMessage(String trackingId, String name, String message);

    // looks for log entries with name or message containing the search term
    @Query("{'trackingId': ?0, '$or': [{'name': {'$regex': ?1, '$options': 'i'}}, {'message': {'$regex': ?1, '$options': 'i'}}]}")
    List<LogEntry> findByNameOrMessageContaining(String trackingId, String searchTerm);
}
