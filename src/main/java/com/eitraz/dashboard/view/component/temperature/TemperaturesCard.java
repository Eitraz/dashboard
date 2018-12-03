package com.eitraz.dashboard.view.component.temperature;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.TemperatureService;
import com.eitraz.dashboard.view.component.CardComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;
import java.util.stream.Collectors;

public class TemperaturesCard extends CardComponent {
    public TemperaturesCard(TemperatureService temperatureService) {
        super(new Icon(VaadinIcon.CLOUD_O), "Temperatur", createContent(temperatureService));
    }

    private static Component createContent(TemperatureService temperatureService) {
        FlexLayout content = new FlexLayout(createTemperatureComponents(temperatureService));
        content.setClassName("temperatures");
        return content;
    }

    private static List<Component> createTemperatureComponents(TemperatureService temperatureService) {
        return temperatureService.getTemperatureSensors().stream()
                                 .map(title -> new TemperatureComponent(temperatureService, title))
                                 .collect(Collectors.toList());
    }
}
