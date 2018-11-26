package com.eitraz.dashboard.service;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
class AccountDetails {
    private String id;
    private String name;
    private String type;
    private Double balance;
}
