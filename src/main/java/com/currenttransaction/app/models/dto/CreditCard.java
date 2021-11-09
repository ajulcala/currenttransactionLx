package com.currenttransaction.app.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCard {
    private String id;
    private String cardNumber;
    private Customer customer;
    private Double limitCredit;
    private Date expiration;
    private Date createAt;
}
