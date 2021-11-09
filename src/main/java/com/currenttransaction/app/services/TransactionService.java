package com.currenttransaction.app.services;

import com.currenttransaction.app.models.documents.Transaction;
import com.currenttransaction.app.models.dto.CurrentAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<Transaction> create(Transaction t);
    Flux<Transaction> findAll();
    Mono<Transaction> findById(String id);
    Mono<Transaction> update(Transaction t);
    Mono<Boolean> delete(String t);
    Mono<Long> countMovements(String t);
    Mono<CurrentAccount> findSavingAccountById(String id);
    Mono<CurrentAccount> updateSavingAccount(CurrentAccount sa);
}
