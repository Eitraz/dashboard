package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = Constants.PAGE_WEATHER, layout = MainLayout.class)
public class WeatherView extends Div {
    public WeatherView() {
        add(new Button("Weather!"));
    }
}
