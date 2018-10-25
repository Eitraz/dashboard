package com.eitraz.dashboard.service;

public class AccountBalance {
    private final String id;
    private final String name;
    private final Double balance;

    public AccountBalance(String id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getBalance() {
        return balance;
    }
}
