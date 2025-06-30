package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.PositionDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PositionRepository extends MongoRepository<PositionDTO, String> {
    boolean existsByClientCodeAndSecurityCodeAndDate(Long clientCode, String securityCode, LocalDate date);

    @Query(value = "{}", sort = "{ date : -1 }")
    PositionDTO findTopByOrderByDateDesc();
}


