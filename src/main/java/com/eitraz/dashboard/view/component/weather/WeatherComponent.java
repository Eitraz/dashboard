package com.eitraz.dashboard.view.component.weather;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.DarkSkyService;
import com.eitraz.dashboard.util.EventPublisher;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import tk.plogitech.darksky.forecast.model.Forecast;

public class WeatherComponent extends FlexLayout {
    private final DarkSkyService darkSkyService;
    private EventPublisher.EventRegistration eventRegistration;

    public WeatherComponent(DarkSkyService darkSkyService) {
        this.darkSkyService = darkSkyService;

        setClassName("weather");
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = UI.getCurrent();
        eventRegistration = darkSkyService.registerEventListener(forecast -> update(ui, forecast));
    }

    private void update(UI ui, Forecast forecast) {
        WeatherDetailsComponent[] components = forecast.getDaily()
                                                       .getData().stream()
                                                       .map(WeatherDetailsComponent::new)
                                                       .toArray(WeatherDetailsComponent[]::new);

        ui.access(() -> {
            removeAll();
            add(components);
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (eventRegistration != null) {
            eventRegistration.deregister();
            eventRegistration = null;
        }
    }
}
