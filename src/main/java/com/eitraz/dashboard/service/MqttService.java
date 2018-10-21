package com.eitraz.dashboard.service;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class MqttService implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final MQTT mqtt;
    private CallbackConnection callbackConnection;
    private boolean isConnected = false;

    private Map<String, List<Consumer<String>>> subscribers = new ConcurrentHashMap<>();
    private Map<String, LocalDateTime> topicTimeout = new HashMap<>();

    public MqttService(@Value("${mqtt.host}") String host,
                       @Value("${mqtt.port}") int port,
                       @Value("${mqtt.username}") String username,
                       @Value("${mqtt.password}") String password) throws URISyntaxException {
        mqtt = new MQTT();
        mqtt.setHost(host, port);
        mqtt.setUserName(username);
        mqtt.setUserName(password);
    }

    @PostConstruct
    public void connect() {
        callbackConnection = mqtt.callbackConnection();
        callbackConnection.listener(this);
        callbackConnection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                logger.debug("Connected");
            }

            @Override
            public void onFailure(Throwable value) {
                logger.debug("Connect error: " + value.getMessage());
            }
        });
    }

    @PreDestroy
    public synchronized void disconnect() {
        if (isConnected) {
            callbackConnection.disconnect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    logger.debug("Disconnected");
                }

                @Override
                public void onFailure(Throwable value) {
                    logger.debug("Disconnect error: " + value.getMessage());
                }
            });
        }
    }

    public MqttSubscriber subscribe(String topic, Consumer<String> consumer) {
        // Subscribe
        if (!subscribers.containsKey(topic)) {
            logger.info(String.format("Subscribing to topic '%s'", topic));

            Topic[] topics = {new Topic(topic, QoS.AT_LEAST_ONCE)};
            callbackConnection.subscribe(topics, new Callback<byte[]>() {
                @Override
                public void onSuccess(byte[] value) {
                }

                @Override
                public void onFailure(Throwable value) {
                }
            });
        }

        List<Consumer<String>> topicSubscribers = subscribers.getOrDefault(topic, new CopyOnWriteArrayList<>());
        topicSubscribers.add(consumer);
        subscribers.put(topic, topicSubscribers);

        return () -> topicSubscribers.remove(consumer);
    }

    @Override
    public synchronized void onConnected() {
        logger.info("Connected");
        isConnected = true;
    }

    @Override
    public synchronized void onDisconnected() {
        logger.info("Disconnected");
        isConnected = false;
    }

    @Override
    public synchronized void onPublish(UTF8Buffer topicBuffer, Buffer body, Runnable ack) {
        String topic = topicBuffer.toString();

        // Prevent spamming
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastEvent = topicTimeout.getOrDefault(topic, LocalDateTime.now().minusDays(1));
        if (now.isBefore(lastEvent.plusSeconds(1)))
            return;

        topicTimeout.put(topic, now);

        String data = body.ascii().toString();

        logger.debug("Received '" + data + "' from topic '" + topic + "'");

        subscribers
                .getOrDefault(topic, new CopyOnWriteArrayList<>())
                .forEach(consumer -> consumer.accept(data));

        ack.run();
    }

    @Override
    public void onFailure(Throwable value) {
        logger.error("Failure", value);
        isConnected = false;
    }

    public interface MqttSubscriber {
        void unsubscribe();
    }
}
