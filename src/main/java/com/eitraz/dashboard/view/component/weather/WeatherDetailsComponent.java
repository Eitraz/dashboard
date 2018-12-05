package com.eitraz.dashboard.view.component.weather;

import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.component.Label;
import com.eitraz.dashboard.util.DateUtils;
import com.vaadin.flow.component.html.Image;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeatherDetailsComponent extends FlexLayout {
    public WeatherDetailsComponent(DailyDataPoint dataPoint) {
        Label date = new Label(getDay(dataPoint.getTime())).withClassNames("date", "text-s");
        Label temperature = new Label(getTemperature(dataPoint)).withClassNames("temp", "text");
        Label summary = new Label(dataPoint.getSummary()).withClassNames("summary", "text-s");

        FlexLayout content = new FlexLayout(date, temperature, summary);
        content.addClassName("content");

        add(getIcon(dataPoint), content);

        addClassName("details");

        getStyle().set("border-left", "var(--lumo-space-xs) solid " + getColor(dataPoint));
    }

    private String getColor(DailyDataPoint dataPoint) {
        double temperature = (dataPoint.getTemperatureLow() + dataPoint.getTemperatureHigh()) / 2d;
        temperature = Math.min(30, Math.max(-30, temperature));

//        double hue = 20 + 240 * Math.round((30 - temperature) / 60 * 10) / 10;
        double hue = 20 + 240 * (30 - temperature) / 60;

        return String.format("hsl(%.0f, 70%%, 50%%)", hue);
    }

    private static String getTemperature(DailyDataPoint dataPoint) {
        return String.format("%.1f °C till %.1f °C", dataPoint.getTemperatureLow(), dataPoint.getTemperatureHigh());
    }

    private static String getDay(Instant time) {
        LocalDate localDate = LocalDateTime.ofInstant(time, DateUtils.EUROPE_STOCKHOLM)
                                           .toLocalDate();
        return DateUtils.getDay(localDate);
    }

    private static Image getIcon(DailyDataPoint dataPoint) {
        Image image = new Image("frontend/images/weather/" + dataPoint.getIcon() + ".svg", "");
        image.addClassName("icon");
        return image;
    }
}
