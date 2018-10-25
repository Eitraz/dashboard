package com.eitraz.dashboard.service.ica;

import com.eitraz.dashboard.service.ica.model.Account;
import com.eitraz.dashboard.service.ica.model.LoginResponse;
import com.eitraz.dashboard.service.ica.model.MinaSidorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import com.vaadin.flow.spring.annotation.SpringComponent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Base64.getEncoder;

@SpringComponent
public class IcaBankenComponent {
    private static final Logger logger = LoggerFactory.getLogger(IcaBankenComponent.class);
    private static final String BASE_URL = "https://api.ica.se/api/";

    @Value("${ica.banken.username}")
    private String username;

    @Value("${ica.banken.password}")
    private String password;

    @SuppressWarnings("FieldCanBeLocal")
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final MeterRegistry registry;
    private final Map<String, AtomicDouble> metrics = new HashMap<>();

    @Autowired
    public IcaBankenComponent(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleWithFixedDelay(this::updateAccounts, 0, 30, TimeUnit.MINUTES);
    }

    private void updateAccounts() {
        try {
            MinaSidorResponse minaSidor = get("user/minasidor/", login(), MinaSidorResponse.class);

            List<Account> accounts = minaSidor.getAccounts();
            accounts.forEach(this::registerMetrics);
        } catch (RuntimeException e) {
            logger.error("Unable to get accounts");
        }
    }

    private void registerMetrics(Account account) {
        String id = DigestUtils.md5Hex(account.getAccountNumber());

        synchronized (metrics) {
            AtomicDouble balance = metrics.get(id);

            // Update
            if (balance != null) {
                balance.set(account.getAvailableAmount());
            }
            // Create new
            else {
                String tag = Normalizer
                        .normalize(account.getName(), Normalizer.Form.NFKD)
                        .replaceAll("\\p{M}", "")
                        .replace(" ", "_")
                        .replace("-", "_")
                        .toLowerCase();

                List<Tag> tags = Arrays.asList(
                        Tag.of("id", id),
                        Tag.of("tag", tag),
                        Tag.of("name", account.getName()),
                        Tag.of("type", "transaction"));

                balance = registry.gauge("economy_account", tags, new AtomicDouble(account.getAvailableAmount()));
                metrics.put(id, balance);
            }
        }
    }

    private String login() {
        String urlString = BASE_URL + "login/";
        logger.info("GET " + urlString);

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                    "Authorization",
                    "Basic " + new String(getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
            connection.connect();

            LoginResponse loginResponse = new ObjectMapper().readValue(connection.getInputStream(), LoginResponse.class);
            logger.info("Login response: " + loginResponse);

            return connection.getHeaderField("AuthenticationTicket");
        } catch (IOException e) {
            logger.error("Unable to get " + urlString, e);

            // Log response
            if (connection != null) {
                try {
                    logger.error("Error response: " + IOUtils.toString(connection.getErrorStream(), StandardCharsets.UTF_8));
                } catch (IOException ignored) {
                }
            }

            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private <T> T get(String path, String authenticationTicket, Class<T> returnType) {
        while (path.startsWith("/") && path.length() > 1) {
            path = path.substring(1);
        }

        String urlString = BASE_URL + path;
        logger.info("GET " + urlString);
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("AuthenticationTicket", authenticationTicket);
            connection.connect();

            T response = new ObjectMapper().readValue(connection.getInputStream(), returnType);
            logger.info("Response: " + response);
            return response;
        } catch (IOException e) {
            logger.error("Unable to get " + urlString, e);

            // Log response
            if (connection != null) {
                try {
                    logger.error("Error response: " + IOUtils.toString(connection.getErrorStream(), StandardCharsets.UTF_8));
                } catch (IOException ignored) {
                }
            }

            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
