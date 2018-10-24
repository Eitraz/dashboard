package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.service.DarkSkyService;
import com.eitraz.dashboard.util.EventPublisher;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;
import tk.plogitech.darksky.forecast.model.Forecast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@StyleSheet("frontend://styles/weather.css")
@SpringComponent
@UIScope
public class DashboardWeatherComponent extends HorizontalLayout {
    private static final Logger logger = LoggerFactory.getLogger(DashboardWeatherComponent.class);

    private final DarkSkyService darkSky;
    private final int numberOfDays;
    private EventPublisher.EventRegistration eventRegistration;
    private List<WeatherEntryComponent> days = Collections.emptyList();
    private UI ui;

    @Autowired
    public DashboardWeatherComponent(DarkSkyService darkSky) {
        this.darkSky = darkSky;
        this.numberOfDays = 4;

        addClassNames("container", "weather");

        days = new ArrayList<>();

        // Create components
        for (int i = 0; i < numberOfDays; i++) {
            days.add(new WeatherEntryComponent(i));
        }

        // Add to layout
        add(days.toArray(new Component[]{}));
        setFlexGrow(2.5, days.get(0));
        setFlexGrow(2, days.get(1));

        for (int i = 2; i < days.size(); i++) {
            setFlexGrow(1, days.get(i));
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = UI.getCurrent();
        eventRegistration = darkSky.registerEventListener(this::update);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (eventRegistration != null) {
            eventRegistration.deregister();
            eventRegistration = null;
        }
    }

    private void update(Forecast forecast) {
        // Detached
        if (ui.getSession() == null)
            return;

        List<DailyDataPoint> dailyDataPoints = forecast.getDaily().getData();

        // Show tomorrow forecast after a specific time of day
        int offset = LocalDateTime.now().isAfter(LocalDateTime.now().withHour(17).withMinute(30)) ? 1 : 0;

        ui.access(() -> {
            logger.debug("Updating weather UI");
            for (int i = 0; i < numberOfDays; i++) {
                days.get(i).update(dailyDataPoints.get(i + offset));
            }
        });
    }
}
