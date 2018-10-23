package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.util.EventPublisher;
import com.eitraz.dashboard.view.component.CalendarDateComponent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.eitraz.dashboard.util.DateUtils.EUROPE_STOCKHOLM;

@StyleSheet("frontend://styles/calendar.css")
@Route(value = Constants.PAGE_CALENDAR, layout = MainLayout.class)
public class CalendarView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(CalendarView.class);

    @Value("${calendar.maxNumberOfEvents}")
    private Integer maxNumberOfEvents;

    private final GoogleCalendarService googleCalendar;

    private EventPublisher.EventRegistration eventRegistration;
    private UI ui;
    private Map<LocalDate, CalendarDateComponent> events = new TreeMap<>();
    private LocalDate lastDate;

    @Autowired
    public CalendarView(GoogleCalendarService googleCalendar) {
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

    private LocalDate getDate(GoogleCalendarService.CalendarEvent event) {
        return Instant.ofEpochMilli(event.getStartTime().getValue())
                      .atZone(EUROPE_STOCKHOLM)
                      .toLocalDate();
    }

    private synchronized void updateCalendarEvents(List<GoogleCalendarService.CalendarEvent> calendarEvents) {
        // Detached
        if (ui.getSession() == null)
            return;

        // Rest on new day
        if (lastDate == null || !LocalDate.now().isEqual(lastDate)) {
            lastDate = LocalDate.now();
            events = new TreeMap<>();
        }

        Map<LocalDate, List<GoogleCalendarService.CalendarEvent>> eventsByDate = calendarEvents
                .stream()
                .collect(Collectors.groupingBy(this::getDate));

        int numberOfEvents = 0;
        for (int dayCounter = 0; dayCounter < 4; dayCounter++) {
            LocalDate date = LocalDate.now().plusDays(dayCounter);

            List<GoogleCalendarService.CalendarEvent> eventsForDate = eventsByDate.getOrDefault(date, new ArrayList<>());

            numberOfEvents += eventsForDate.size();

            if (numberOfEvents > maxNumberOfEvents)
                break;

            // Update
            if (events.containsKey(date)) {
                events.get(date).setCalendarEvents(eventsForDate);
            }
            // Create
            else {
                events.put(date, new CalendarDateComponent(date, eventsForDate));
            }
        }

        ui.access(() -> {
            logger.debug("Updating calender UI");
            removeAll();
            add(events.values().toArray(new CalendarDateComponent[]{}));
            events.values().forEach(CalendarDateComponent::update);
        });
    }
}
