package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.util.DateUtils;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.eitraz.dashboard.util.DateUtils.EUROPE_STOCKHOLM;
import static com.eitraz.dashboard.util.DateUtils.getTime;

public class CalendarEventComponent extends HorizontalLayout {
    private GoogleCalendarService.CalendarEvent calendarEvent;
    private final Label label;

    public CalendarEventComponent(GoogleCalendarService.CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;

        label = new Label();
        add(label);
    }

    public GoogleCalendarService.CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public void setCalendarEvent(GoogleCalendarService.CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;
    }

    public void update() {
        String calendarId = calendarEvent.getCalendarId();
        Event event = calendarEvent.getEvent();

        String summary = event.getSummary();
        DateTime startTime = calendarEvent.getStartTime();
        DateTime endTime = (!Boolean.FALSE.equals(event.getEndTimeUnspecified()) && event.getEnd() != null && event.getEnd().getDateTime() != null) ? event.getEnd().getDateTime() : null;

        // Date
        LocalDate date = Instant.ofEpochMilli(startTime.getValue())
                                .atZone(EUROPE_STOCKHOLM)
                                .toLocalDate();

        String time;

        // Date only
        if (startTime.isDateOnly()) {
            time = DateUtils.getDay(date);
        }
        // Date and time
        else {
            // Start time
            LocalDateTime startDateTime = Instant.ofEpochMilli(startTime.getValue())
                                                 .atZone(EUROPE_STOCKHOLM)
                                                 .toLocalDateTime();

            // End time
            LocalDateTime endDateTime;
            if (endTime != null) {
                endDateTime = Instant.ofEpochMilli(endTime.getValue())
                                     .atZone(EUROPE_STOCKHOLM)
                                     .toLocalDateTime();
            } else {
                endDateTime = null;
            }


            time = DateUtils.getDay(date) + ", " + getTime(startDateTime, endDateTime);
        }

        // Category
        String category;
        if (calendarId.contains(".") && !calendarId.contains("@group")) {
            category = calendarId.substring(0, calendarId.indexOf("."));
        } else {
            category = "other";
        }

        setClassName("entry");
        addClassNames(category);

        // Update text
        label.setText(StringUtils.abbreviate(time + " - " + summary, 72));
    }
}
