package com.eitraz.dashboard.component;

public class Label extends com.vaadin.flow.component.html.Label {
    public Label() {
    }

    public Label(String text) {
        super(text);
    }

    public Label withClassName(String className) {
        addClassName(className);
        return this;
    }

    public Label withClassNames(String... classNames) {
        addClassNames(classNames);
        return this;
    }
}
