package com.eitraz.dashboard.view.component.economy;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.AccountBalance;

import java.util.List;

public class EconomyComponent extends FlexLayout {
    public EconomyComponent(List<AccountBalance> accounts) {
        setClassName("economy");

        add(accounts.stream()
                    .map(AccountComponent::new)
                    .toArray(AccountComponent[]::new));
    }
}
