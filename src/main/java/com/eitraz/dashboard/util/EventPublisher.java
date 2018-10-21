package com.eitraz.dashboard.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class EventPublisher<T> {
    @Autowired
    private ApplicationEventPublisher publisher;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public synchronized EventRegistration registerEventListener(Consumer<T> listener) {
        listeners.add(listener);
        eventListenerRegistered(listener);

        return () -> {
            synchronized (EventRegistration.class) {
                listeners.remove(listener);
            }
        };
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected synchronized void eventListenerRegistered(Consumer<T> listener) {
    }

    protected synchronized void broadcastEvent(T event) {
        // Send to Spring components
        publisher.publishEvent(event);

        // Send to Vaadin listeners
        for (Consumer<T> listener : listeners) {
            executor.execute(() -> listener.accept(event));
        }
    }

    public interface EventRegistration {
        void deregister();
    }

}
