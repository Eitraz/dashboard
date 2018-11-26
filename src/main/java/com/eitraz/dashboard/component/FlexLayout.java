package com.eitraz.dashboard.component;

import com.vaadin.flow.component.Component;

import java.util.List;

public class FlexLayout extends com.vaadin.flow.component.orderedlayout.FlexLayout {
    public FlexLayout() {
    }

    public FlexLayout(Component... children) {
        super(children);
    }

    public FlexLayout(List<Component> children) {
        super(children.toArray(new Component[0]));
    }

    public FlexLayout withClassName(String className) {
        addClassName(className);
        return this;
    }

    public FlexLayout withClassNames(String... classNames) {
        addClassNames(classNames);
        return this;
    }
}
