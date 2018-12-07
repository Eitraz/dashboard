package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.DarkSkyService;
import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.service.TemperatureService;
import com.eitraz.dashboard.view.component.calendar.CalendarCard;
import com.eitraz.dashboard.view.component.temperature.TemperaturesCard;
import com.eitraz.dashboard.view.component.weather.WeatherCard;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Route(value = Constants.PAGE_ROOT, layout = MainLayout.class)
public class DashboardView extends FlexLayout {
    @Autowired
    public DashboardView(TemperatureService temperatureService, GoogleCalendarService googleCalendar, DarkSkyService darkSkyService) {
        addClassNames("dashboard", "dashboard_view");

        add(new TemperaturesCard(temperatureService));

        FlexLayout columns = new FlexLayout(
                new CalendarCard(googleCalendar, 10),
                new WeatherCard(darkSkyService));
        columns.addClassName("columns");
        add(columns);
    }
}
