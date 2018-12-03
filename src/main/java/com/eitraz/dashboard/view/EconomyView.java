package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.AccountBalance;
import com.eitraz.dashboard.service.AvanzaService;
import com.eitraz.dashboard.service.IcaBankenService;
import com.eitraz.dashboard.service.SwedbankService;
import com.eitraz.dashboard.util.EventPublisher;
import com.eitraz.dashboard.view.component.economy.AvanzaTotpDialog;
import com.eitraz.dashboard.view.component.economy.EconomyCard;
import com.eitraz.dashboard.view.component.economy.PasswordLoginDialog;
import com.eitraz.dashboard.view.component.economy.SwedbankLoginDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@StyleSheet("frontend://styles/economy.css")
@Route(value = Constants.PAGE_ECONOMY, layout = MainLayout.class)
public class EconomyView extends FlexLayout {
    private final SwedbankService swedbank;
    private final IcaBankenService icaBanken;
    private final AvanzaService avanza;
    private final SwedbankLoginDialog swedbankLoginDialog;

    @Value("${economy.accounts}")
    private String accounts;

    @Value("${economy.password}")
    private String password;

    private EventPublisher.EventRegistration accountsUpdatedRegistration;

    @Autowired
    public EconomyView(SwedbankService swedbank, IcaBankenService icaBanken, AvanzaService avanza, SwedbankLoginDialog swedbankLoginDialog) {
        this.swedbank = swedbank;
        this.icaBanken = icaBanken;
        this.avanza = avanza;
        this.swedbankLoginDialog = swedbankLoginDialog;

        setClassName("dashboard");
    }

    private void updateAccounts(UI ui) {
        List<AccountBalance> accountBalances = new ArrayList<>();
        accountBalances.addAll(swedbank.getAccountsBalance());
        accountBalances.addAll(icaBanken.getAccountsBalance());
        accountBalances.addAll(avanza.getAccountsBalance());

        List<AccountBalance> visibleAccounts = Arrays
                .stream(accounts.split(","))
                .map(id -> accountBalances.stream().filter(a -> id.equals(a.getId())).findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        ui.access(() -> {
            removeAll();
            add(new EconomyCard(visibleAccounts));
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        removeAll();

        UI ui = UI.getCurrent();

        accountsUpdatedRegistration = swedbankLoginDialog.registerAccountsUpdatedListener(() -> updateAccounts(ui));

        // Ask for TOTP
        if (avanza.isWaitingForTotp()) {
            AvanzaTotpDialog avanzaTotpDialog = new AvanzaTotpDialog(avanza::setTotp);
            avanzaTotpDialog.open();

            // Open Swedbank Login on close
            avanzaTotpDialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    // Open Swedbank Login
                    if (!swedbank.isLoggedIn()) {
                        ui.access(swedbankLoginDialog::open);
                    }
                    // Use password
                    else {
                        loginUsingPassword(ui);
                    }
                }
            });
        }
        // Open Swedbank Login
        else if (!swedbank.isLoggedIn()) {
            swedbankLoginDialog.open();
        }
        // Use password
        else {
            loginUsingPassword(ui);
        }
    }

    private void loginUsingPassword(UI ui) {
        new PasswordLoginDialog(password, () -> updateAccounts(ui)).open();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (accountsUpdatedRegistration != null) {
            accountsUpdatedRegistration.deregister();
            accountsUpdatedRegistration = null;
        }
    }

}
