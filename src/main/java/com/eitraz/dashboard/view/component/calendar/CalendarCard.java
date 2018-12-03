package com.eitraz.dashboard.view.component.calendar;

import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.view.component.CardComponent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class CalendarCard extends CardComponent {
    public CalendarCard(GoogleCalendarService googleCalendar, int numberOfEvents) {
        super(new Icon(VaadinIcon.CALENDAR), "Kalender", new CalendarComponent(googleCalendar, numberOfEvents));
    }
}
