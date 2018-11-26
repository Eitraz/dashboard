package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.component.Button;
import com.eitraz.dashboard.component.Label;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ErrorDialog extends Dialog {
    public ErrorDialog(String message) {
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        Label label = new Label(message);

        // Cancel button
        Button closeButton = new Button("Close", event -> close());

        VerticalLayout layout = new VerticalLayout(label, closeButton);
        layout.setWidth("100%");
        layout.setPadding(false);
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        add(layout);
    }

    public static void showErrorDialog(String message) {
        new ErrorDialog(message).open();
    }
}
