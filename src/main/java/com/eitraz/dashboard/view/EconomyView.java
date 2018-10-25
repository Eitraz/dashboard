package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.AccountBalance;
import com.eitraz.dashboard.service.SwedbankService;
import com.eitraz.dashboard.service.ica.IcaBankenService;
import com.eitraz.dashboard.util.DateUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@StyleSheet("frontend://styles/economy.css")
@Route(value = Constants.PAGE_ECONOMY, layout = MainLayout.class)
public class EconomyView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(EconomyView.class);

    private final SwedbankService swedbank;
    private final IcaBankenService icaBanken;

    @Value("${economy.usernames}")
    private String usernames;

    @Value("${economy.accounts}")
    private String accounts;

    @Value("${economy.password}")
    private String password;

    @Autowired
    public EconomyView(SwedbankService swedbank, IcaBankenService icaBanken) {
        this.swedbank = swedbank;
        this.icaBanken = icaBanken;
        setClassName("economy");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // Logged in
        if (swedbank.isLoggedIn()) {
            addPasswordLogin();
        }
        // Login
        else {
            addMobileBankIdLoginButtons();
        }
    }

    private void addPasswordLogin() {
        PasswordField passwordField = new PasswordField();
        passwordField.setRevealButtonVisible(false);
        passwordField.setRequired(true);
        passwordField.setErrorMessage("Invalid password");

        Button button = new Button("Login");
        button.addClassName("loginButton");
        button.getElement().setAttribute("theme", "primary");
        button.addClickListener(event -> {
            // OK
            if (password.equals(passwordField.getValue())) {
                getAccountsBalance();
            }
            // Invalid password
            else {
                passwordField.setInvalid(true);
            }
        });

        // Layout
        HorizontalLayout loginButtonsLayout = new HorizontalLayout(passwordField, button);
        loginButtonsLayout.setClassName("loginButtons");

        addClassName("login");
        add(loginButtonsLayout);
    }

    private void addMobileBankIdLoginButtons() {
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

    private void getAccountsBalance() {
        Dialog loadAccountsBalance = new Dialog();
        loadAccountsBalance.setCloseOnEsc(false);
        loadAccountsBalance.setCloseOnOutsideClick(false);

        // Label
        loadAccountsBalance.add(new Label("Loading accounts..."));

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        loadAccountsBalance.add(progressBar);

        UI ui = UI.getCurrent();

        // Get accounts balance
        swedbank.getAccountsBalance(
                // Accounts balance
                accountsBalance -> ui.access(() -> {
                    if (loadAccountsBalance.isOpened()) {
                        loadAccountsBalance.close();
                        displayAccounts(accountsBalance);
                    }
                }),
                // Failed
                failedMessage -> ui.access(() -> {
                    if (loadAccountsBalance.isOpened()) {
                        loadAccountsBalance.close();
                        Notification.show(failedMessage, 2500, Notification.Position.MIDDLE);
                    }
                }));

        // Cancel button
        Button cancelButton = new Button("Cancel", event -> loadAccountsBalance.close());
        loadAccountsBalance.add(cancelButton);

        // Open
        loadAccountsBalance.open();
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

        // Login
        SwedbankService.Task loginTask = swedbank.loginWithBankId(personalNumber,
                // Logged in
                () -> ui.access(() -> {
                    if (loginDialog.isOpened()) {
                        loginDialog.close();
                        getAccountsBalance();
                    }
                }),
                // Failed
                failedMessage -> ui.access(() -> {
                    if (loginDialog.isOpened()) {
                        loginDialog.close();
                        Notification.show(failedMessage, 2500, Notification.Position.MIDDLE);
                    }
                }));

        // Cancel button
        Button cancelButton = new Button("Cancel", event -> {
            loginTask.abort();
            loginDialog.close();
        });
        loginDialog.add(cancelButton);

        // Open
        loginDialog.open();
    }

    private void displayAccounts(List<AccountBalance> swedbankAccountsBalance) {
        List<AccountBalance> accountsBalance = new ArrayList<>();

        // Add Swedbank
        accountsBalance.addAll(swedbankAccountsBalance);

        // Add ICA
        accountsBalance.addAll(icaBanken.getAccountsBalance());

        logger.info("Display accounts balance");

        List<String> visibleAccounts = Arrays.asList(accounts.split(","));

        removeAll();

        VerticalLayout accountsLayout = new VerticalLayout();
        accountsLayout.addClassName("accountsList");
        accountsLayout.setWidth("40%");

        accountsBalance.stream()
                       .filter(account -> visibleAccounts.contains(account.getId()))
                       .sorted(Comparator.comparing(AccountBalance::getName))
                       .map(account -> new AccountComponent(account.getName(), account.getBalance()))
                       .forEach(accountsLayout::add);

        setClassName("economy");
        addClassName("accounts");
        add(accountsLayout);
    }

    private class AccountComponent extends HorizontalLayout {
        AccountComponent(String name, Double balance) {
            addClassName("account");

            Label nameLabel = new Label(StringUtils.capitalize(name));
            nameLabel.addClassName("name");

            String amount = NumberFormat.getNumberInstance(DateUtils.SWEDEN).format(balance);
            Label balanceLabel = new Label(String.format("%s kr", amount));
            balanceLabel.addClassName("balance");

            add(nameLabel, balanceLabel);
        }
    }
}
