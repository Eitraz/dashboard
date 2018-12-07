package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.component.Div;
import com.eitraz.dashboard.service.DarkSkyService;
import com.eitraz.dashboard.view.component.weather.WeatherCard;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Route(value = Constants.PAGE_WEATHER, layout = MainLayout.class)
public class WeatherView extends Div {

    @Autowired
    public WeatherView(DarkSkyService darkSky) {
        addClassNames("dashboard", "weather_view");
        add(new WeatherCard(darkSky));
    }
}
