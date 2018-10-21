package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.service.TemperatureService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;

public class TemperatureComponent extends VerticalLayout {
    private final TemperatureService temperatureService;
    private final String temperatureSensorName;
    private TemperatureService.TemperatureListenerRegistration registration;
    private UI ui;

    private final Label temperatureLabel;

    public TemperatureComponent(TemperatureService temperatureService, String temperatureSensorName) {
        this.temperatureService = temperatureService;
        this.temperatureSensorName = temperatureSensorName;

        // Title
        Label title = new Label(StringUtils.capitalize(temperatureSensorName));
        title.setClassName("title");

        // Value
        temperatureLabel = new Label("-- °C");
        temperatureLabel.setClassName("value");

        // Bottom
        HorizontalLayout bottomLayout = new HorizontalLayout(title, temperatureLabel);
        bottomLayout.setClassName("bottom");
        bottomLayout.setPadding(true);
        add(bottomLayout);

        String className = Normalizer
                .normalize(temperatureSensorName, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replace(" ", "_")
                .toLowerCase();

        addClassNames("container", className);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = UI.getCurrent();
        registration = temperatureService.registerTemperatureListener(temperatureSensorName, this::updateTemperature);
    }

    private void updateTemperature(Double temperature) {
        // Detached
        if (ui.getSession() == null)
            return;

        ui.access(() -> temperatureLabel.setText(String.format("%.1f °C", temperature).replace(",", ".")));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (registration != null) {
            registration.deregister();
            registration = null;
        }
    }
}
