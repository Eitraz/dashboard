package com.eitraz.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.Objects;

@PageTitle("Dashboard")
@StyleSheet("frontend://styles/style.css")
@StyleSheet("frontend://styles/temperatures.css")
@StyleSheet("frontend://styles/calendar.css")
@StyleSheet("frontend://styles/economy.css")
@StyleSheet("frontend://styles/surveillance.css")
@HtmlImport("frontend://src/lumo.html")
@Theme(value = Lumo.class, variant = Lumo.DARK)
@Push(value = PushMode.AUTOMATIC)
public class MainLayout extends FlexLayout implements RouterLayout {

    private Component content;

    public MainLayout() {
        setClassName("layout");
        add(new MainMenu());
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (this.content != null) {
            remove(this.content);
            this.content = null;
        }

        if (content != null) {
            this.content = Objects.requireNonNull((Component) content);
            add(this.content);
        }
    }
}
