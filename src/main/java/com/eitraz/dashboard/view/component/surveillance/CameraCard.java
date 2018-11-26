package com.eitraz.dashboard.view.component.surveillance;

import com.eitraz.dashboard.view.component.CardComponent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class CameraCard extends CardComponent {
    public CameraCard(String title, String url) {
        super(null, title, new CameraComponent(url));
    }
}
