package com.eitraz.dashboard.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

public class CardComponent extends FlexLayout {
    public CardComponent(Icon icon, String title, Component content) {
        // Title
        Label titleLabel = new Label(title);
        titleLabel.addClassNames("title", "text");

        // Header
        FlexLayout header = new FlexLayout();

        // Icon
        if (icon != null) {
            icon.addClassName("icon");
            header.add(icon);
        }

        header.add(titleLabel);
        header.setClassName("header");

        // Card
        setClassName("card");
        add(header, content);
    }
}
