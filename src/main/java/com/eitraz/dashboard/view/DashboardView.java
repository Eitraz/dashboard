package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.service.TemperatureService;
import com.eitraz.dashboard.view.component.calendar.CalendarCard;
import com.eitraz.dashboard.view.component.temperature.TemperaturesCard;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Route(value = Constants.PAGE_ROOT, layout = MainLayout.class)
public class DashboardView extends FlexLayout {
    @Autowired
    public DashboardView(TemperatureService temperatureService, GoogleCalendarService googleCalendar) {
        setClassName("dashboard");

        add(new TemperaturesCard(temperatureService));
        add(new CalendarCard(googleCalendar, 10));
    }
}
