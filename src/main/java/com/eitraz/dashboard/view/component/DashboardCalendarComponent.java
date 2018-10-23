package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.util.EventPublisher;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

@StyleSheet("frontend://styles/calendar.css")
@SpringComponent
@UIScope
public class DashboardCalendarComponent extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(DashboardCalendarComponent.class);

    private final GoogleCalendarService googleCalendar;
    private EventPublisher.EventRegistration eventRegistration;

    private Map<String, CalendarEventComponent> events = Collections.emptyMap();
    private UI ui;

    @Value("${dashboard.calendar.numberOfEvents}")
    private Integer numberOfEvents;

    @Autowired
    public DashboardCalendarComponent(GoogleCalendarService googleCalendar) {
        this.googleCalendar = googleCalendar;
        addClassNames("container", "calendar");
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
            add(events.values().toArray(new CalendarEventComponent[]{}));
            events.values().forEach(CalendarEventComponent::update);
        });
    }

}
