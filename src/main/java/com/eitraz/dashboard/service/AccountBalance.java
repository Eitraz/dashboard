package com.eitraz.dashboard.service;

import lombok.Getter;

@Getter
public class AccountBalance {
    private final String id;
    private final String name;
    private final Double balance;

    AccountBalance(String id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }
}
