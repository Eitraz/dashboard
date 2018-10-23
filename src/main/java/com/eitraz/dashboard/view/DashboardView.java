package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.TemperatureService;
import com.eitraz.dashboard.view.component.DashboardCalendarComponent;
import com.eitraz.dashboard.view.component.DashboardTemperatureComponent;
import com.eitraz.dashboard.view.component.DashboardWeatherComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Route(value = Constants.PAGE_ROOT, layout = MainLayout.class)
public class DashboardView extends FlexLayout {
    @Autowired
    public DashboardView(DashboardCalendarComponent calendar, DashboardWeatherComponent weather, TemperatureService temperatureService) {
        setClassName("dashboard");

        // Group temperatures in rows with 3 columns
        int columns = 3;
        AtomicInteger temperatureCounter = new AtomicInteger(0);
        Collection<List<String>> temperatureSensorRows = temperatureService
                .getTemperatureSensors().stream()
                .collect(Collectors.groupingBy(o -> temperatureCounter.getAndIncrement() / columns))
                .values();

        // Add each temperature row
        temperatureSensorRows.forEach(row -> {
            // Temperature 1
            HorizontalLayout layout = new HorizontalLayout();
            layout.setMargin(true);
            layout.setClassName("row");
            setFlexGrow(1, layout);

            row.forEach(temperatureSensor ->
                    layout.add(new DashboardTemperatureComponent(temperatureService, temperatureSensor)));

            DashboardView.this.add(layout);
        });

        // Calendar / weather
        HorizontalLayout calendarLayout = new HorizontalLayout(calendar, weather);
        calendarLayout.setClassName("row");
        calendarLayout.setMargin(true);
        setFlexGrow(2.5, calendarLayout);
        add(calendarLayout);
    }
}
