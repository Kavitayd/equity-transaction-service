package com.equity.transaction.service.repository;

import com.equity.transaction.service.model.MarketPriceDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketPriceRepository extends MongoRepository<MarketPriceDTO, String> {
}
