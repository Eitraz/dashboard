package com.eitraz.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class TemperatureService {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureService.class);

    private final MqttService mqtt;
    private final Map<Integer, String> temperatureIds;

    private List<MqttService.MqttSubscriber> subscribers;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private Map<String, List<Consumer<Double>>> listeners = new HashMap<>();

    private Map<Integer, String> cache = new HashMap<>();

    @Autowired
    public TemperatureService(Environment environment, MqttService mqtt) {
        this.mqtt = mqtt;
        this.temperatureIds = new LinkedHashMap<>();

        int notSet = 0;
        int i = 0;
        while (notSet++ < 10) {
            String value = environment.getProperty("mqtt.temperature." + (i++));
            if (value != null) {
                if (value.contains("=")) {
                    String[] values = value.split("=");
                    temperatureIds.put(Integer.parseInt(values[1]), values[0]);
                }

                notSet = 0;
            }
        }
    }

    private String getTopicName(Integer id) {
        return String.format("sensor/%d/temperature", id);
    }

    public Collection<String> getTemperatureSensors() {
        return Collections.unmodifiableCollection(temperatureIds.values());
    }

    @PostConstruct
    public void subscribe() {
        subscribers = temperatureIds
                .keySet().stream()
                .map(id -> mqtt.subscribe(getTopicName(id), value -> publish(id, value, true)))
                .collect(Collectors.toList());

        publishCache();
    }

    private synchronized void publishCache() {
        cache.forEach((key, value) -> publish(key, value, false));
    }

    @SuppressWarnings("CodeBlock2Expr")
    private synchronized void publish(Integer id, String value, boolean cache) {
        try {
            Double temperature = Double.parseDouble(value);

            Optional.ofNullable(temperatureIds.get(id))
                    .ifPresent(name -> {
                        listeners.getOrDefault(name, new ArrayList<>())
                                 .forEach(listener -> {
                                     executor.execute(() -> listener.accept(temperature));
                                 });
                    });

            if (cache) {
                this.cache.put(id, value);
            }
        } catch (NumberFormatException e) {
            logger.error("Unable to parse value '" + value + "' as double for id " + id);
        }
    }

    @PreDestroy
    public void unsubscribe() {
        if (subscribers != null) {
            subscribers.forEach(MqttService.MqttSubscriber::unsubscribe);
            subscribers = null;
        }
    }

    public TemperatureListenerRegistration registerTemperatureListener(String name, Consumer<Double> temperatureConsumer) {
        List<Consumer<Double>> listenerList = listeners.getOrDefault(name, new ArrayList<>());
        listenerList.add(temperatureConsumer);
        listeners.put(name, listenerList);

        return () -> {
            synchronized (TemperatureListenerRegistration.class) {
                listenerList.remove(temperatureConsumer);
            }
        };
    }

    @FunctionalInterface
    public interface TemperatureListenerRegistration {
        void deregister();
    }
}
