package com.eitraz.dashboard.view.component.economy;

import com.eitraz.dashboard.service.AccountBalance;
import com.eitraz.dashboard.view.component.CardComponent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;

public class EconomyCard extends CardComponent {
    public EconomyCard(List<AccountBalance> accounts) {
        super(new Icon(VaadinIcon.DOLLAR), "Ekonomi", new EconomyComponent(accounts));
    }
}
