package com.eitraz.dashboard.view.component.economy;

import com.eitraz.dashboard.component.Button;
import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.SwedbankService;
import com.eitraz.dashboard.util.EventPublisher;
import com.github.eitraz.swedbank.SwedbankApi;
import com.github.eitraz.swedbank.authentication.MobileBankId;
import com.github.eitraz.swedbank.bank.BankType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.eitraz.dashboard.view.component.ErrorDialog.showErrorDialog;

@UIScope
@SpringComponent
public class SwedbankLoginDialog extends Dialog {
    private static final Logger logger = LoggerFactory.getLogger(SwedbankLoginDialog.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final SwedbankService swedbank;
    private final MobileBankId mobileBankId = new MobileBankId(BankType.SWEDBANK);

    private final List<Runnable> accountsUpdatedListeners = new ArrayList<>();

    @Autowired
    public SwedbankLoginDialog(SwedbankService swedbank, @Value("${economy.usernames}") String usernames) {
        this.swedbank = swedbank;

        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        Map<String, String> users = Arrays.stream(usernames.split(","))
                                          .map(user -> user.split(":"))
                                          .collect(Collectors.toMap(
                                                  s -> s[0],
                                                  s -> s[1]
                                          ));

        Component[] buttons = users
                .entrySet().stream()
                .map(user ->
                        new Button(user.getKey(), event -> login(user.getValue()))
                                .withThemes("primary")
                                .withStyle("margin", "var(--lumo-space-s)")
                )
                .toArray(Component[]::new);


        FlexLayout layout = new FlexLayout(buttons);
        layout.setFlexGrow(1, buttons);

        add(layout);
    }

    public synchronized EventPublisher.EventRegistration registerAccountsUpdatedListener(Runnable listener) {
        accountsUpdatedListeners.add(listener);

        return () -> {
            synchronized (EventPublisher.EventRegistration.class) {
                accountsUpdatedListeners.remove(listener);
            }
        };
    }

    private void login(String username) {
        close();

        // Display message to login using Mobile BankID
        LoginWithMobileBankId loginWithMobileBankIdDialog = new LoginWithMobileBankId();
        loginWithMobileBankIdDialog.open();

        UI ui = UI.getCurrent();
        executorService.execute(() -> {
            SwedbankApi api;

            try {
                Future<SwedbankApi> authenticate = mobileBankId.authenticate(username);

                // Authenticate
                api = authenticate.get(30, TimeUnit.SECONDS);
            }
            // Login already in progress
            catch (RejectedExecutionException e) {
                logger.error("Login already in progress", e);
                ui.access(() -> showErrorDialog("A Mobile BankID login is already in progress - please wait at least 30 seconds and try again"));
                return;
            }
            // Failed to login
            catch (Throwable e) {
                logger.error("Failed to authenticate with Swedbank", e);
                ui.access(() -> showErrorDialog("Failed to authenticate with Swedbank: " + e.getMessage()));
                return;
            } finally {
                ui.access(loginWithMobileBankIdDialog::close);
            }

            // Display updating accounts dialog
            UpdateAccountsDialog updateAccountsDialog = new UpdateAccountsDialog();
            ui.access(updateAccountsDialog::open);

            try {
                this.swedbank.login(api);
                this.swedbank.updateAccounts();

                // Notify listeners
                accountsUpdatedListeners.forEach(Runnable::run);
            } catch (Throwable e) {
                logger.error("Error while updating accounts", e);
                ui.access(() -> showErrorDialog("Error while updating accounts: " + e.getMessage()));
            } finally {
                ui.access(updateAccountsDialog::close);
            }
        });
    }

    private class LoginWithMobileBankId extends Dialog {
        LoginWithMobileBankId() {
            setCloseOnEsc(false);
            setCloseOnOutsideClick(false);

            Label label = new Label("Login with Mobile BankID");

            // Progress bar
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);

            // Cancel button
            Button cancelButton = new Button("Cancel", event -> LoginWithMobileBankId.this.close());

            VerticalLayout layout = new VerticalLayout(label, progressBar, cancelButton);
            layout.setWidth("100%");
            layout.setPadding(false);
            layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
            add(layout);
        }
    }

    private class UpdateAccountsDialog extends Dialog {
        UpdateAccountsDialog() {
            setCloseOnEsc(false);
            setCloseOnOutsideClick(false);

            Label label = new Label("Updating accounts...");

            // Progress bar
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);


            VerticalLayout layout = new VerticalLayout(label, progressBar);
            layout.setWidth("100%");
            layout.setPadding(false);
            layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
            add(layout);
        }
    }
}
