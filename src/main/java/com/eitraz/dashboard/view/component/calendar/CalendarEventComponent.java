package com.eitraz.dashboard.view.component.calendar;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.component.Label;
import com.eitraz.dashboard.service.GoogleCalendarService;
import com.eitraz.dashboard.util.DateUtils;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.eitraz.dashboard.util.DateUtils.EUROPE_STOCKHOLM;
import static com.eitraz.dashboard.util.DateUtils.getTime;

public class CalendarEventComponent extends FlexLayout {
    private final boolean displayDate = true;
    private final Label dateLabel;
    private final Label label;

    private GoogleCalendarService.CalendarEvent calendarEvent;

    public CalendarEventComponent(GoogleCalendarService.CalendarEvent calendarEvent) {
        setCalendarEvent(calendarEvent);

        dateLabel = new Label().withClassNames("date", "text-xs");
        label = new Label().withClassNames("label", "text");

        add(dateLabel, label);

        setClassName("event");
    }

    GoogleCalendarService.CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    void setCalendarEvent(GoogleCalendarService.CalendarEvent calendarEvent) {
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
            time = displayDate ? (DateUtils.getDay(date)) : "";
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

            // With date
            if (displayDate) {
                time = DateUtils.getDay(date) + ", " + getTime(startDateTime, endDateTime);
            }
            // Without date
            else {
                time = getTime(startDateTime, endDateTime);
            }
        }

        // Category
        String category;
        if (calendarId.contains(".") && !calendarId.contains("@group")) {
            category = calendarId.substring(0, calendarId.indexOf("."));
        } else {
            category = "other";
        }

        setClassName("event");
        addClassNames(category);

        // Update text
        dateLabel.setText(time);
        label.setText(summary);
    }
}
