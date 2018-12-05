package com.eitraz.dashboard.view.component.surveillance;

import com.eitraz.dashboard.component.FlexLayout;
import com.vaadin.flow.component.html.Image;

public class CameraComponent extends FlexLayout {
    public CameraComponent(String url) {
        setClassName("camera");

        add(new Image(url, ""));
    }
}
