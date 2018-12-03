package com.eitraz.dashboard.view;

import com.eitraz.dashboard.Constants;
import com.eitraz.dashboard.MainLayout;
import com.eitraz.dashboard.component.FlexLayout;
import com.eitraz.dashboard.view.component.surveillance.CameraCard;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

@Route(value = Constants.PAGE_SURVEILLANCE, layout = MainLayout.class)
public class SurveillanceView extends FlexLayout {
    public SurveillanceView(Environment environment) {
        for (int i = 0; i < 100; i++) {
            String title = environment.getProperty(String.format("camera.%d.title", i));
            String url = environment.getProperty(String.format("camera.%d.url", i));

            if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(url)) {
                add(new CameraCard(title, url));
            }
        }

        setClassName("dashboard");
        addClassName("surveillance");
    }
}
