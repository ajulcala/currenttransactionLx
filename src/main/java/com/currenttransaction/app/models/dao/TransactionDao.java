package com.currenttransaction.app.models.dao;

import com.currenttransaction.app.models.documents.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionDao extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByCurrentAccountId(String id);
}
