package com.currenttransaction.app.services.impl;

import com.currenttransaction.app.models.dao.TransactionDao;
import com.currenttransaction.app.models.documents.Transaction;
import com.currenttransaction.app.models.dto.CurrentAccount;
import com.currenttransaction.app.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
public class TransactionServiceImpl implements TransactionService {
    private final WebClient webClient;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;
    @Value("${config.base.apigatewey}")
    private String url;

    public TransactionServiceImpl(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = WebClient.builder().baseUrl(this.url).build();
        this.reactiveCircuitBreaker = circuitBreakerFactory.create("current");
    }

    @Autowired
    TransactionDao dao;

    @Override
    public Mono<CurrentAccount> findSavingAccountById(String id) {
        return reactiveCircuitBreaker.run(webClient.get().uri(this.url + "/find/{id}",id).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(CurrentAccount.class),
                throwable -> {return this.getDefaultCurrentAccount();});
    }

    @Override
    public Mono<CurrentAccount> updateSavingAccount(CurrentAccount sa) {
        return reactiveCircuitBreaker.run(webClient.put().uri(this.url + "/update",sa.getId()).accept(MediaType.APPLICATION_JSON).bodyValue(sa).retrieve().bodyToMono(CurrentAccount.class),
                throwable -> { return this.getDefaultCurrentAccount();});
    }

    public Mono<CurrentAccount> getDefaultCurrentAccount() {
        Mono<CurrentAccount> currentAccount = Mono.just(new CurrentAccount("0",null,null,null,null,null,null,null,null,null));
        return currentAccount;
    }

    @Override
    public Mono<Transaction> create(Transaction t) {
        return dao.save(t);
    }

    @Override
    public Flux<Transaction> findAll() {
        return dao.findAll();
    }

    @Override
    public Mono<Transaction> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<Transaction> update(Transaction t) {
        return dao.save(t);
    }

    @Override
    public Mono<Boolean> delete(String t) {
        return dao.findById(t)
                .flatMap(ca -> dao.delete(ca).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Long> countMovements(String t) {
        return dao.findByCurrentAccountId(t).count();
    }
}
