package com.eitraz.dashboard.view.component.economy;

import com.eitraz.dashboard.component.Button;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class PasswordLoginDialog extends Dialog {
    public PasswordLoginDialog(String password, Runnable loginSuccessRunnable) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        // TOTP
        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("Password");
        passwordField.setValueChangeMode(ValueChangeMode.EAGER);
        passwordField.setRequired(true);
        passwordField.setAutofocus(true);

        // Cancel
        Button cancelButton = new Button("Cancel", event -> PasswordLoginDialog.this.close());
        cancelButton.setWidth("50%");

        // Submit
        Button submitButton = new Button("Submit", event -> {
            if (password.equals(passwordField.getValue())) {
                passwordField.setInvalid(false);
                PasswordLoginDialog.this.close();
                loginSuccessRunnable.run();
            }
            // Show error message
            else {
                passwordField.setErrorMessage("Invalid password");
                passwordField.setInvalid(true);
            }
        });
        submitButton.withThemes("primary");
        submitButton.setWidth("50%");

        // Enter submits
        passwordField.addKeyPressListener(Key.ENTER, event -> submitButton.click());

        // Button row
        HorizontalLayout buttonRow = new HorizontalLayout(cancelButton, submitButton);
        buttonRow.setFlexGrow(1, cancelButton, submitButton);
        buttonRow.setWidth("100%");

        // Layout
        VerticalLayout layout = new VerticalLayout(passwordField, buttonRow);
        add(layout);
    }
}
