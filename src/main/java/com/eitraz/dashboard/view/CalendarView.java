package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.view.component.calendar.CalendarCard;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Route(value = Constants.PAGE_CALENDAR, layout = MainLayout.class)
public class CalendarView extends FlexLayout {
    @Autowired
    public CalendarView(GoogleCalendarService googleCalendar) {
        setClassName("dashboard");
        add(new CalendarCard(googleCalendar, 20));
    }
}
