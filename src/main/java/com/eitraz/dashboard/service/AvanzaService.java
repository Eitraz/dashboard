package com.eitraz.dashboard.service;

import com.github.eitraz.avanza.AvanzaApi;
import com.github.eitraz.avanza.model.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Service
public class AvanzaService extends AbstractAccountService {
    private static final Logger logger = LoggerFactory.getLogger(AvanzaService.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final AvanzaApi avanza;
    private final BlockingDeque<String> totpQueue = new LinkedBlockingDeque<>();
    private boolean waitingForTotp;

    @Autowired
    public AvanzaService(@Value("${avanza.username}") String username,
                         @Value("${avanza.password}") String password) {
        avanza = new AvanzaApi(username, password, () -> {
            try {
                totpQueue.clear();
                waitingForTotp = true;
                return totpQueue.take();
            } catch (InterruptedException e) {
                logger.error("Totp take interrupted");
                return "";
            } finally {
                waitingForTotp = false;
            }
        });
    }

    public boolean isWaitingForTotp() {
        return waitingForTotp;
    }

    public void setTotp(String totp) {
        totpQueue.offer(totp);
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleWithFixedDelay(this::updateAccounts, 0, 5, TimeUnit.MINUTES);
    }

    private void updateAccounts() {
        try {
            avanza.getOverview()
                  .getAccounts()
                  .forEach(account -> registerMetrics(accountToAccountDetails(account)));
        } catch (RuntimeException e) {
            logger.error("Unable to get accounts");
        }
    }

    private AccountDetails accountToAccountDetails(Account account) {
        return new AccountDetails()
                .setId(account.getAccountId())
                .setName(account.getName())
                .setType(account.getAccountType())
                .setBalance(account.getOwnCapital());
    }
}
