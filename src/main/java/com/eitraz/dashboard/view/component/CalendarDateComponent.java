package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.util.DateUtils;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarDateComponent extends HorizontalLayout {
    private VerticalLayout calendarEventsLayout;
    private Map<String, CalendarEventComponent> events = Collections.emptyMap();

    public CalendarDateComponent(LocalDate date, List<GoogleCalendarService.CalendarEvent> calendarEvents) {
        addClassNames("calendar", "date_container");

        String day = StringUtils.capitalize(DateUtils.getDay(date, true, "EEEE"));
        Label dateLabel = new Label(day);

        VerticalLayout dateLayout = new VerticalLayout(dateLabel);
        dateLayout.addClassNames("date");
        dateLayout.setWidth("10%");

        // Calendar Events
        calendarEventsLayout = new VerticalLayout();
        calendarEventsLayout.addClassNames("container", "calendar");
        calendarEventsLayout.setWidth("90%");

        add(dateLayout, calendarEventsLayout);
        setFlexGrow(1, calendarEventsLayout);

        setCalendarEvents(calendarEvents);
    }

    public void setCalendarEvents(List<GoogleCalendarService.CalendarEvent> calendarEvents) {
        events = calendarEvents
                .stream()
                .map(calendarEvent -> {
                    if (events.containsKey(calendarEvent.getId())) {
                        CalendarEventComponent component = events.get(calendarEvent.getId());
                        component.setCalendarEvent(calendarEvent);
                        return component;
                    } else {
                        return new CalendarEventComponent(calendarEvent, false, 200);
                    }
                })
                .sorted(Comparator.comparingLong(o -> o.getCalendarEvent().getStartTime().getValue()))
                .collect(Collectors.toMap(
                        o -> o.getCalendarEvent().getId(),
                        o -> o,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }

    public void update() {
        calendarEventsLayout.removeAll();
        calendarEventsLayout.add(events.values().toArray(new CalendarEventComponent[]{}));
        events.values().forEach(CalendarEventComponent::update);
    }
}
