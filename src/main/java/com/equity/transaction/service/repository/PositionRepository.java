package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.PositionDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends MongoRepository<PositionDTO, String> {
}
