package com.eitraz.dashboard.view.component.calendar;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.util.EventPublisher;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CalendarComponent extends FlexLayout {
    private static final Logger logger = LoggerFactory.getLogger(CalendarComponent.class);

    private final GoogleCalendarService googleCalendar;
    private EventPublisher.EventRegistration eventRegistration;

    private Map<String, CalendarEventComponent> events = Collections.emptyMap();
    private UI ui;
    private final int numberOfEvents;

    public CalendarComponent(GoogleCalendarService googleCalendar, int numberOfEvents) {
        this.googleCalendar = googleCalendar;
        this.numberOfEvents = numberOfEvents;

        setClassName("calendar");
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        ui = UI.getCurrent();
        eventRegistration = googleCalendar.registerEventListener(this::updateCalendarEvents);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (eventRegistration != null) {
            eventRegistration.deregister();
            eventRegistration = null;
        }
    }

    private void updateCalendarEvents(List<GoogleCalendarService.CalendarEvent> calendarEvents) {
        // Detached
        if (ui.getSession() == null)
            return;

        events = calendarEvents
                .stream()
                .map(calendarEvent -> {
                    if (events.containsKey(calendarEvent.getId())) {
                        CalendarEventComponent component = events.get(calendarEvent.getId());
                        component.setCalendarEvent(calendarEvent);
                        return component;
                    } else {
                        return new CalendarEventComponent(calendarEvent);
                    }
                })
                .sorted(Comparator.comparingLong(o -> o.getCalendarEvent().getStartTime().getValue()))
                .limit(numberOfEvents)
                .collect(Collectors.toMap(
                        o -> o.getCalendarEvent().getId(),
                        o -> o,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        ui.access(() -> {
            logger.debug("Updating calender UI");
            removeAll();
            add(events.values().toArray(new CalendarEventComponent[0]));
            events.values().forEach(CalendarEventComponent::update);
        });
    }
}
