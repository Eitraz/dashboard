package com.eitraz.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
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
@StyleSheet("frontend://styles/site.css")
@Theme(value = Lumo.class, variant = Lumo.DARK)
@Push(value = PushMode.AUTOMATIC)
public class MainLayout extends FlexLayout implements RouterLayout {

    private final FlexLayout content;

    public MainLayout() {
        setClassName("layout");

        add(new MainMenu());

        content = new FlexLayout();
        content.setClassName("content");
        add(content);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        if (content != null) {
            this.content.removeAll();
            this.content.add(Objects.requireNonNull((Component) content));
        }
    }
}
