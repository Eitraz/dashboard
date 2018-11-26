package com.eitraz.dashboard.view.component.temperature;

import com.eitraz.dashboard.component.Div;
import com.eitraz.dashboard.component.FlexLayout;

class TemperatureBarComponent extends FlexLayout {
    private final Double min;
    private final Double max;

    private final Div minus;
    private final Div plus;

    TemperatureBarComponent(Double min, Double max) {
        this.min = min;
        this.max = max;

        // Minus
        minus = new Div()
                .withClassName("minus_value")
                .withWidth("0%");

        Div minusLayout = new Div(minus)
                .withClassName("minus")
                .withStyle("flex", (Math.abs(min) / (-min + max) * 100) + " auto");

        // Plus
        plus = new Div()
                .withClassName("plus_value")
                .withWidth("0%");

        Div plusLayout = new Div(plus)
                .withClassName("plus")
                .withStyle("flex", (max / (-min + max) * 100) + " auto");

        // Bar
        FlexLayout bar = new FlexLayout(minusLayout, plusLayout)
                .withClassName("bar");

        setClassName("bar_container");
        add(bar);
    }

    void setTemperature(Double temperature) {
        minus.setWidth((Math.min(0, Math.max(min, temperature)) / min * 100) + "%");
        plus.setWidth((Math.max(0, Math.min(max, temperature)) / max * 100) + "%");
    }
}
