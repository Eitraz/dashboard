package com.eitraz.dashboard.view.component.surveillance;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.AccountBalance;
import com.eitraz.dashboard.view.component.economy.AccountComponent;
import com.vaadin.flow.component.html.Image;

import java.util.List;

public class CameraComponent extends FlexLayout {
    public CameraComponent(String url) {
        setClassName("camera");

        add(new Image(url, ""));
    }
}
