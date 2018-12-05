package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.component.Div;
import com.eitraz.dashboard.service.DarkSkyService;
import com.eitraz.dashboard.view.component.weather.WeatherCard;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@StyleSheet("frontend://styles/weather.css")
@Route(value = Constants.PAGE_WEATHER, layout = MainLayout.class)
public class WeatherView extends Div {

    @Autowired
    public WeatherView(DarkSkyService darkSky) {
        setClassName("dashboard");
        add(new WeatherCard(darkSky));
    }
}
