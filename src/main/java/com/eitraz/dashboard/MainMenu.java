package com.eitraz.dashboard;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

import java.util.ArrayList;

@HtmlImport("frontend://src/menu.html")
public class MainMenu extends FlexLayout implements AfterNavigationObserver {
    private ArrayList<String> links = new ArrayList<>();
    private String currentLink;
    private ArrayList<Button> buttons = new ArrayList<>();

    public MainMenu() {
        setClassName("menu");
        init();
    }

    private void init() {
        addMenuItem(Constants.PAGE_ROOT, VaadinIcon.HOME);
        addMenuItem(Constants.PAGE_CALENDAR, VaadinIcon.CALENDAR);
        addMenuItem(Constants.PAGE_WEATHER, VaadinIcon.CLOUD_O);
        addMenuItem(Constants.PAGE_ECONOMY, VaadinIcon.DOLLAR);
        addMenuItem(Constants.PAGE_SURVEILLANCE, VaadinIcon.CAMERA);
    }

    private void addMenuItem(String link, VaadinIcon vaadinIcon) {
        Icon icon = new Icon(vaadinIcon);
        icon.setClassName("icon");
//        icon.setSize("40px");
        icon.setColor("white");

        Button button = new Button(icon);
        button.addClassName("button");
        button.getElement().setAttribute("theme", "primary tertiary");

        buttons.add(button);
        add(button);

        button.addClickListener(event -> {
            if (!link.equals(currentLink)) {
                UI.getCurrent().navigate(link);
            }
        });

        links.add(link);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String link = event.getLocation().getFirstSegment();

        if (link.isEmpty())
            link = Constants.PAGE_ROOT;

        currentLink = link;

        //        buttons.get(links.indexOf(link)).addClassName("selected");

        buttons.forEach(button -> button.getElement().setAttribute("theme", "primary tertiary"));
        buttons.get(links.indexOf(link)).getElement().setAttribute("theme", "primary");
    }
}
