package com.eitraz.dashboard.service;

import com.github.eitraz.ica.IcaBankenApi;
import com.github.eitraz.ica.model.user.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class IcaBankenService extends AbstractAccountService {
    private static final Logger logger = LoggerFactory.getLogger(IcaBankenService.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final IcaBankenApi ica;

    @Autowired
    public IcaBankenService(@Value("${ica.banken.username}") String username,
                            @Value("${ica.banken.password}") String password) {
        this.ica = new IcaBankenApi(username, password);
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleWithFixedDelay(this::updateAccounts, 0, 15, TimeUnit.MINUTES);
    }

    private void updateAccounts() {
        try {
            ica.getMinaSidor()
               .getAccounts()
               .forEach(account -> registerMetrics(accountToAccountDetails(account)));
        } catch (RuntimeException e) {
            logger.error("Unable to get accounts");
        }
    }

    private AccountDetails accountToAccountDetails(Account account) {
        return new AccountDetails()
                .setId(account.getAccountNumber())
                .setName(account.getName())
                .setType("transaction")
                .setBalance(account.getAvailableAmount());
    }
}
