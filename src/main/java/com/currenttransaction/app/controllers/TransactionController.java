package com.currenttransaction.app.controllers;

import com.currenttransaction.app.models.documents.Transaction;
import com.currenttransaction.app.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RefreshScope
@RestController
@RequestMapping("/transactionCurrentAccount")
public class TransactionController {
    @Autowired
    TransactionService service;

    @GetMapping("list")
    public Flux<Transaction> findAll(){
        return service.findAll();
    }

    @GetMapping("/find/{id}")
    public Mono<Transaction> findById(@PathVariable String id){
        return service.findById(id);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Transaction>> create(@RequestBody Transaction transaction){
        // VERIFICAMOS SI EXISTE EL CLIENTE
        return service.countMovements(transaction.getCurrentAccount().getId()) // N° Movimientos actuales
                .flatMap(cnt -> {
                    return service.findSavingAccountById(transaction.getCurrentAccount().getId()) // Busco la Cuenta Bancaria
                            .flatMap(ca -> {
                                switch (transaction.getTypeTransaction()){
                                    case DEPOSIT: ca.setBalance(ca.getBalance() + transaction.getTransactionAmount()); break;
                                    case DRAFT: ca.setBalance(ca.getBalance() - transaction.getTransactionAmount()); break;
                                }
                                if(cnt >= ca.getFreeTransactions() ){
                                    ca.setBalance(ca.getBalance() - ca.getCommissionTransactions());
                                    transaction.setCommissionAmount(ca.getCommissionTransactions());
                                }else{
                                    transaction.setCommissionAmount(0.0);
                                }
                                return service.updateSavingAccount(ca)
                                        .flatMap(saveCa -> {
                                            transaction.setCurrentAccount(saveCa);
                                            transaction.setTransactionDate(LocalDateTime.now());
                                            return service.create(transaction); // Mono<TransactionCurrentAccount>
                                        }); // Mono<CurrentAccount>
                            });
                })
                .map(tca -> new ResponseEntity<>(tca, HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<Transaction>> update(@RequestBody Transaction transaction) {
        return service.findById(transaction.getId())
                .flatMap(tcaDB -> service.findSavingAccountById(transaction.getCurrentAccount().getId())
                        .flatMap(ca -> {
                            switch (transaction.getTypeTransaction()){
                                case DEPOSIT: ca.setBalance(ca.getBalance() - tcaDB.getTransactionAmount() + transaction.getTransactionAmount());
                                    return service.updateSavingAccount(ca).flatMap(caUpdate -> {
                                        transaction.setCurrentAccount(caUpdate);
                                        transaction.setTransactionDate(LocalDateTime.now());
                                        return service.create(transaction); // Mono<TransactionCurrentAccount>
                                    }); // Mono<CurrentAccount>
                                case DRAFT: ca.setBalance(ca.getBalance() + tcaDB.getTransactionAmount() - transaction.getTransactionAmount());
                                    return service.updateSavingAccount(ca).flatMap(caUpdate -> {
                                        transaction.setCurrentAccount(caUpdate);
                                        transaction.setTransactionDate(LocalDateTime.now());
                                        return service.create(transaction); // Mono<TransactionCurrentAccount>
                                    }); // Mono<CurrentAccount>
                                default: return Mono.empty();
                            }
                        })
                )
                .map(tca -> new ResponseEntity<>(tca, HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return service.delete(id)
                .filter(deleteCustomer -> deleteCustomer)
                .map(deleteCustomer -> new ResponseEntity<>("Transaction Deleted", HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
