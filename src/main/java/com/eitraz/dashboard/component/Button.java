package com.eitraz.dashboard.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class Button extends com.vaadin.flow.component.button.Button {

    public Button() {
    }

    public Button(String text) {
        super(text);
    }

    public Button(Component icon) {
        super(icon);
    }

    public Button(String text, Component icon) {
        super(text, icon);
    }

    public Button(String text, ComponentEventListener<ClickEvent<com.vaadin.flow.component.button.Button>> clickListener) {
        super(text, clickListener);
    }

    public Button(Component icon, ComponentEventListener<ClickEvent<com.vaadin.flow.component.button.Button>> clickListener) {
        super(icon, clickListener);
    }

    public Button(String text, Component icon, ComponentEventListener<ClickEvent<com.vaadin.flow.component.button.Button>> clickListener) {
        super(text, icon, clickListener);
    }

    public Button withClassName(String className) {
        addClassName(className);
        return this;
    }

    public Button withClassNames(String... classNames) {
        addClassNames(classNames);
        return this;
    }

    public Button withThemes(String... themes) {
        getElement().setAttribute("theme", stream(themes).collect(joining(" ")));
        return this;
    }

    public Button withStyle(String name, String value) {
        getElement().getStyle().set(name, value);
        return this;
    }
}
