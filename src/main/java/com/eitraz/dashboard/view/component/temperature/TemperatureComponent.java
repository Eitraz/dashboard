package com.eitraz.dashboard.view.component.temperature;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.component.Label;
import com.eitraz.dashboard.service.TemperatureService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureComponent extends FlexLayout {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureComponent.class);

    private final TemperatureService temperatureService;
    private final String temperatureSensorName;
    private TemperatureService.TemperatureListenerRegistration registration;
    private UI ui;

    private final TemperatureBarComponent bar;
    private final Label temperatureLabel;

    TemperatureComponent(TemperatureService temperatureService, String temperatureSensorName) {
        this.temperatureService = temperatureService;
        this.temperatureSensorName = temperatureSensorName;

        Label titleLabel = new Label(temperatureSensorName)
                .withClassNames("title", "text");

        bar = new TemperatureBarComponent(-20d, 30d);

        temperatureLabel = new Label("-- °C")
                .withClassNames("value", "text");

        addClassName("temperature");
        add(titleLabel, bar, temperatureLabel);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        logger.trace("onAttach");

        ui = UI.getCurrent();
        registration = temperatureService.registerTemperatureListener(temperatureSensorName, this::updateTemperature);
    }

    private void updateTemperature(Double temperature) {
        // Detached
        if (ui.getSession() == null)
            return;

        ui.access(() -> {
            bar.setTemperature(temperature);
            temperatureLabel.setText(String.format("%.1f °C", temperature).replace(",", "."));
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        logger.trace("onDetach");

        if (registration != null) {
            registration.deregister();
            registration = null;
        }
    }

}
