package com.eitraz.dashboard.service;

import com.eitraz.dashboard.util.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.plogitech.darksky.api.jackson.DarkSkyJacksonClient;
import tk.plogitech.darksky.forecast.*;
import tk.plogitech.darksky.forecast.model.Forecast;
import tk.plogitech.darksky.forecast.model.Latitude;
import tk.plogitech.darksky.forecast.model.Longitude;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class DarkSkyService extends EventPublisher<Forecast> {
    private static final Logger logger = LoggerFactory.getLogger(DarkSkyService.class);

    @Value("${darksky.api.key}")
    private String apiKey;

    @Value("${darksky.latitude}")
    private Double latitude;

    @Value("${darksky.longitude}")
    private Double longitude;

    @Value("${darksky.updateDelayInMinutes}")
    private Integer updateDelayInMinutes;

    @SuppressWarnings("FieldCanBeLocal")
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private Forecast lastForecast = null;

    public DarkSkyService() {
    }

    @PostConstruct
    public void init() {
        // Schedule updates
        scheduledExecutorService.scheduleWithFixedDelay(
                this::updateForecast, 0, updateDelayInMinutes, TimeUnit.MINUTES);
    }

    @Override
    protected synchronized void eventListenerRegistered(Consumer<Forecast> listener) {
        if (lastForecast != null) {
            listener.accept(lastForecast);
        }
    }

    private synchronized void updateForecast() {
        logger.info("Updating forecast");

        ForecastRequest request = new ForecastRequestBuilder()
                .key(new APIKey(apiKey))
                .language(ForecastRequestBuilder.Language.sv)
                .units(ForecastRequestBuilder.Units.si)
                .location(new GeoCoordinates(new Longitude(longitude), new Latitude(latitude))).build();

        DarkSkyJacksonClient client = new DarkSkyJacksonClient();

        try {
            lastForecast = client.forecast(request);
            logger.info("Forecast updated");

            broadcastEvent(lastForecast);
        } catch (ForecastException e) {
            logger.error("Failed to update forecast", e);
        }
    }
}
