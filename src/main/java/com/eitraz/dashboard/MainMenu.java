package com.eitraz.dashboard;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

import java.util.ArrayList;

@HtmlImport("frontend://src/menu.html")
public class MainMenu extends FlexLayout implements AfterNavigationObserver {
    private ArrayList<String> links = new ArrayList<>();
    private String currentLink;

    private Tabs menu;

    public MainMenu() {
        setClassName("menu");
        init();
    }

    private void init() {
        menu = new Tabs();
        menu.setClassName("items");

        addMenuItem(Constants.PAGE_ROOT, VaadinIcon.HOME);
        addMenuItem(Constants.PAGE_CALENDAR, VaadinIcon.CALENDAR);
        addMenuItem(Constants.PAGE_WEATHER, VaadinIcon.SUN_O);
        addMenuItem(Constants.PAGE_ECONOMY, VaadinIcon.DOLLAR);

        menu.addSelectedChangeListener(event -> {
            int index = menu.getSelectedIndex();
            if (index >= 0 && index < links.size()) {
                String link = links.get(index);

                if (!link.equals(currentLink)) {
                    UI.getCurrent().navigate(link);
                }
            }
        });
        add(menu);
    }

    private void addMenuItem(String link, VaadinIcon vaadinIcon) {
        Icon icon = new Icon(vaadinIcon);
        icon.setClassName("menu-icon");
        icon.setSize("40px");
        icon.setColor("white");

        links.add(link);

        Tab tab = new Tab(icon);
        tab.setClassName("item");

        menu.add(tab);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String link = event.getLocation().getFirstSegment();

        if (link.isEmpty())
            link = Constants.PAGE_ROOT;

        currentLink = link;
        menu.setSelectedIndex(links.indexOf(link));
    }
}
