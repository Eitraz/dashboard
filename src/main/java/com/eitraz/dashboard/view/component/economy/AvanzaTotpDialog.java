package com.eitraz.dashboard.view.component.economy;

import com.eitraz.dashboard.component.Button;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AvanzaTotpDialog extends Dialog {
    public AvanzaTotpDialog(Consumer<String> totpConsumer) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        // TOTP
        PasswordField totpField = new PasswordField();
        totpField.setPlaceholder("Avanza TOTP");
        totpField.setValueChangeMode(ValueChangeMode.EAGER);
        totpField.setRequired(true);
        totpField.setAutofocus(true);
        totpField.setMaxLength(6);
        totpField.setPattern("[0-9]*");
        totpField.setPreventInvalidInput(true);

        // Cancel
        Button cancelButton = new Button("Cancel", event -> AvanzaTotpDialog.this.close());
        cancelButton.setWidth("50%");

        // Submit
        Button submitButton = new Button("Submit", event -> {
            totpConsumer.accept(totpField.getValue());
            AvanzaTotpDialog.this.close();
        });
        submitButton.withThemes("primary");
        submitButton.setEnabled(false);
        submitButton.setWidth("50%");

        // TOTP value
        totpField.addValueChangeListener(event -> {
            String value = StringUtils.trimToEmpty(event.getValue());

            boolean isValid = Pattern.matches("\\d{6}", value);
            submitButton.setEnabled(isValid);
        });

        // Enter submits
        totpField.addKeyPressListener(Key.ENTER, event -> submitButton.click());

        // Button row
        HorizontalLayout buttonRow = new HorizontalLayout(cancelButton, submitButton);
        buttonRow.setFlexGrow(1, cancelButton, submitButton);
        buttonRow.setWidth("100%");

        // Layout
        VerticalLayout layout = new VerticalLayout(totpField, buttonRow);
        add(layout);
    }
}
