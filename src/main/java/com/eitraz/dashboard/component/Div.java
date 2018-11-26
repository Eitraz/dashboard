package com.eitraz.dashboard.component;

import com.vaadin.flow.component.Component;

public class Div extends com.vaadin.flow.component.html.Div {
    public Div() {
    }

    public Div(Component... components) {
        super(components);
    }

    public Div(String text) {
        setText(text);
    }

    public Div withClassName(String className) {
        addClassName(className);
        return this;
    }

    public Div withClassNames(String... classNames) {
        addClassNames(classNames);
        return this;
    }

    public Div withWidth(String width) {
        setWidth(width);
        return this;
    }

    public Div withStyle(String name, String value) {
        getElement().getStyle().set(name, value);
        return this;
    }
}
