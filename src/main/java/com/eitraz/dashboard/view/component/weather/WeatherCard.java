package com.eitraz.dashboard.view.component.weather;

import com.eitraz.dashboard.service.DarkSkyService;
import com.eitraz.dashboard.view.component.CardComponent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class WeatherCard extends CardComponent {
    public WeatherCard(DarkSkyService darkSkyService) {
        super(new Icon(VaadinIcon.CLOUD_O), "Väder", new WeatherComponent(darkSkyService));
    }
}
