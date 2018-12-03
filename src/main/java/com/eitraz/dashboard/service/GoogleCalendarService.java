package com.eitraz.dashboard.service;

import com.eitraz.dashboard.util.EventPublisher;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService extends EventPublisher<List<GoogleCalendarService.CalendarEvent>> {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    @Value("${google.calendar.application.name}")
    private String applicationName;

    @Value("${google.calendar.client.id}")
    private String clientId;

    @Value("${google.calendar.client.secret}")
    private String clientSecret;

    @Value("${google.calendar.ids}")
    private String calendarIds;

    @Value("${google.calendar.updateDelayInMinutes}")
    private Integer updateDelayInMinutes;

    @Value("${google.calendar.tokensDirectoryPath}")
    private String tokensDirectoryPath;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private Calendar service;
    private List<String> calendarIdList;
    private List<CalendarEvent> lastCalendarEvents = null;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        calendarIdList = Arrays.asList(calendarIds.split(","));

        // Build a new authorized API client service.
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        service = new Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(applicationName)
                .build();

        // Schedule updates
        scheduledExecutorService.scheduleWithFixedDelay(
                this::updateCalendarEvents, 0, updateDelayInMinutes, TimeUnit.MINUTES);
    }

    @Override
    protected synchronized void eventListenerRegistered(Consumer<List<GoogleCalendarService.CalendarEvent>> listener) {
        if (lastCalendarEvents != null) {
            listener.accept(lastCalendarEvents);
        }
    }

    private synchronized void updateCalendarEvents() {
        logger.info("Updating calendar events");

        try {
            lastCalendarEvents = calendarIdList.stream()
                                               .map(this::getEvents)
                                               .flatMap(Collection::stream)
                                               .sorted(Comparator.comparingLong(o -> o.getStartTime().getValue()))
                                               .collect(Collectors.toList());

            logger.info("Calendar events updated");
            broadcastEvent(lastCalendarEvents);
        } catch (Exception e) {
            logger.error("Failed to update calendar events", e);
        }
    }

    /**
     * Get calendar events for calendarId
     */
    private synchronized List<CalendarEvent> getEvents(String calendarId) {
        try {
            return service.events()
                          .list(calendarId)
                          .setMaxResults(20)
                          .setTimeMin(new DateTime(System.currentTimeMillis()))
                          .setOrderBy("startTime")
                          .setSingleEvents(true)
                          .execute()
                          .getItems()
                          .stream()
                          .map(event -> new CalendarEvent(calendarId, event))
                          .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        {
            GoogleClientSecrets.Details installed = new GoogleClientSecrets.Details();
            installed.setClientId(clientId);
            installed.setClientSecret(clientSecret);
            clientSecrets.setInstalled(installed);
        }

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }


    public class CalendarEvent {
        final String calendarId;
        final Event event;

        CalendarEvent(String calendarId, Event event) {
            this.calendarId = calendarId;
            this.event = event;
        }

        public String getId() {
            return getCalendarId() + "." + event.getId();
        }

        public String getCalendarId() {
            return calendarId;
        }

        public Event getEvent() {
            return event;
        }

        public DateTime getStartTime() {
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
            return start;
        }
    }
}
