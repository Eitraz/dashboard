package com.eitraz.dashboard.view.component;

import com.eitraz.dashboard.util.DateUtils;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import tk.plogitech.darksky.forecast.model.DailyDataPoint;

import java.time.LocalDate;

class WeatherEntryComponent extends FlexLayout {
    private final Div icon;
    private final Label temperatureHigh;
    private final Label temperatureLow;
    private Label summary;
    private final LocalDate date;

    WeatherEntryComponent(int index, DailyDataPoint data) {
        date = LocalDate.from(data.getTime().atZone(DateUtils.EUROPE_STOCKHOLM));
        String day = StringUtils.capitalize(DateUtils.getDay(date, true, "EEEE"));

        Label title = new Label(day);
        title.addClassNames("title");

        icon = new Div();
        icon.addClassNames("icon");

        temperatureHigh = new Label();
        temperatureHigh.addClassNames("temperature", "high");

        temperatureLow = new Label();
        temperatureLow.addClassNames("temperature", "low");

        if (index == 0) {
            summary = new Label();
            summary.addClassNames("summary");
        }

        VerticalLayout temperature = new VerticalLayout(temperatureHigh, temperatureLow);
        temperature.addClassName("temperatures");

        if (summary != null)
            temperature.add(summary);

        add(title, icon, temperature);
        addClassNames("layout", day.toLowerCase().replace(" ", "_"), "day" + index);
        setFlexGrow(1, icon);

        update(data);
    }

    LocalDate getDate() {
        return date;
    }

    void update(DailyDataPoint data) {
        temperatureHigh.setText(String.format("%.0f °C", data.getTemperatureHigh()));
        temperatureLow.setText(String.format("%.0f °C", data.getTemperatureLow()));

        if (summary != null) {
            summary.setText(data.getSummary());
        }

        icon.setClassName("icon");
        icon.addClassName(data.getIcon());
    }
}
