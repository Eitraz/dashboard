package com.eitraz.dashboard.view.component.economy;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.component.Label;
import com.eitraz.dashboard.service.AccountBalance;
import com.eitraz.dashboard.util.DateUtils;

import java.text.NumberFormat;

public class AccountComponent extends FlexLayout {
    public AccountComponent(AccountBalance account) {
        String balance = NumberFormat.getNumberInstance(DateUtils.SWEDEN)
                                     .format(account.getBalance());

        add(
                new Label(account.getName()).withClassNames("name", "text-xs"),
                new Label(String.format("%s kr", balance)).withClassNames("balance", "text")
        );

        setClassName("account");
    }
}
