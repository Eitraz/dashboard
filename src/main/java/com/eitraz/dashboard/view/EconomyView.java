package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.SwedbankService;
import com.eitraz.dashboard.util.DateUtils;
import com.github.soshibby.swedbank.types.Account;
import com.github.soshibby.swedbank.types.TransactionAccount;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@StyleSheet("frontend://styles/economy.css")
@Route(value = Constants.PAGE_ECONOMY, layout = MainLayout.class)
public class EconomyView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(EconomyView.class);

    private final SwedbankService swedbank;

    @Value("${economy.usernames}")
    private String usernames;

    @Value("${economy.accounts}")
    private String accounts;

    @Autowired
    public EconomyView(SwedbankService swedbank) {
        this.swedbank = swedbank;
        setClassName("economy");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        HorizontalLayout loginButtonsLayout = new HorizontalLayout();
        loginButtonsLayout.setClassName("loginButtons");

        Arrays.stream(usernames.split(","))
              .map(username -> {
                  String[] split = username.split(":");
                  return createLoginButton(split[0], split[1]);
              })
              .forEach(loginButtonsLayout::add);

        addClassName("login");
        add(loginButtonsLayout);
    }

    private Component createLoginButton(String name, String personalNumber) {
        Button button = new Button(name);
        button.addClassName("loginButton");
        button.getElement().setAttribute("theme", "primary");
        button.addClickListener(event -> login(personalNumber));
        return button;
    }

    private void login(String personalNumber) {
        Dialog loginDialog = new Dialog();
        loginDialog.setCloseOnEsc(false);
        loginDialog.setCloseOnOutsideClick(false);

        // Label
        loginDialog.add(new Label("Login with Mobile Bank ID"));

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        loginDialog.add(progressBar);

        UI ui = UI.getCurrent();

        // Start task
        SwedbankService.Task getAccountsTask = swedbank.getAccounts(personalNumber, transactionAccounts -> {
            ui.access(() -> {
                loginDialog.close();
                displayAccounts(transactionAccounts);
            });
        });

        // Cancel button
        Button cancelButton = new Button("Cancel", event -> {
            getAccountsTask.abort();
            loginDialog.close();
        });
        loginDialog.add(cancelButton);

        // Open
        loginDialog.open();

    }

    private void displayAccounts(List<TransactionAccount> transactionAccounts) {
        if (transactionAccounts == null)
            return;

        List<String> visibleAccounts = Arrays.asList(accounts.split(","));

        removeAll();

        VerticalLayout accountsLayout = new VerticalLayout();
        accountsLayout.addClassName("accountsList");
        accountsLayout.setWidth("40%");

        transactionAccounts.stream()
                           .filter(account -> visibleAccounts.contains(account.getName()))
                           .sorted(Comparator.comparing(Account::getName))
                           .map(AccountComponent::new)
                           .forEach(accountsLayout::add);

        setClassName("economy");
        addClassName("accounts");
        add(accountsLayout);
    }

    private class AccountComponent extends HorizontalLayout {
        AccountComponent(TransactionAccount account) {
            addClassName("account");

            Label name = new Label(StringUtils.capitalize(account.getName()));
            name.addClassName("name");

            String amount = NumberFormat.getNumberInstance(DateUtils.SWEDEN).format(account.getBalance());
            Label balance = new Label(String.format("%s kr", amount));
            balance.addClassName("balance");

            add(name, balance);
        }
    }
}
