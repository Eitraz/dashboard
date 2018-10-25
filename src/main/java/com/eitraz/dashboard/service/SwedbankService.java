package com.eitraz.dashboard.service;

import com.github.soshibby.swedbank.Swedbank;
import com.github.soshibby.swedbank.app.SwedbankApp;
import com.github.soshibby.swedbank.authentication.MobileBankID;
import com.github.soshibby.swedbank.exceptions.SwedbankAuthenticationException;
import com.github.soshibby.swedbank.types.AccountList;
import com.github.soshibby.swedbank.types.TransactionAccount;
import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class SwedbankService {
    private static final Logger logger = LoggerFactory.getLogger(SwedbankService.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final MeterRegistry registry;

    // id -> balance (also act as lock for metrics and accountNames)
    private final Map<String, AtomicDouble> metrics = new HashMap<>();

    // id -> name
    private final Map<String, String> accountNames = new HashMap<>();

    private final String lock = "lock";
    private Swedbank swedbank;

    private LocalDateTime lastUpdateAccountsTime = LocalDateTime.now().minusDays(1);

    @Autowired
    public SwedbankService(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
//        scheduledExecutorService.scheduleWithFixedDelay(this::updateAccounts, 0, 10, TimeUnit.MINUTES);
    }

    private boolean updateAccounts() {
        logger.info("Update accounts");

        synchronized (lock) {
            try {
                if (isLoggedIn()) {
                    LocalDateTime now = LocalDateTime.now();
                    // No need to fetch to often
                    if (now.isBefore(lastUpdateAccountsTime.plusMinutes(5))) {
                        logger.info("No need to fetch accounts");
                        return true;
                    }

                    // TODO: Disable temporary until schedule update is fixed
                    //lastUpdateAccountsTime = now;

                    logger.info("Fetching accounts");

                    AccountList accountList = swedbank.accountList();
                    List<TransactionAccount> accounts = accountList.getAllAccounts();
                    logger.info("Accounts fetched, registering metrics");

                    // Register metrics
                    accounts.forEach(account -> registerMetrics(accountList, account));

                    // Need to log out for data to be refreshed? TODO: Look into this
                    swedbank = null;

                    return true;
                } else {
                    logger.info("Not logged in, won't fetch accounts");
                    return false;
                }
            } catch (Throwable e) {
                logger.error("Error while fetching accounts", e);
                swedbank = null;
                return false;
            }
        }
    }

    public boolean isLoggedIn() {
        synchronized (lock) {
            try {
                return swedbank != null && swedbank.isLoggedIn();
            } catch (SwedbankAuthenticationException e) {
                return false;
            }
        }
    }

    public Task loginWithBankId(String personalNumber, Runnable loggedIn, Consumer<String> failed) {
        synchronized (lock) {
            swedbank = new Swedbank();
            MobileBankID mobileBankID = new MobileBankID(new SwedbankApp(), personalNumber);

            Task task = new LoginTask();
            executorService.execute(() -> {
                try {
                    swedbank.login(mobileBankID);

                    while (!task.isAborted() && !swedbank.isLoggedIn()) {
                        Thread.sleep(5000);
                    }

                    // Force update accounts
                    if (!task.isAborted()) {
                        loggedIn.run();
                    }
                } catch (Throwable e) {
                    logger.error("Failed to login", e);
                    failed.accept("Failed to login");
                }
            });
            return task;
        }
    }

    public void getAccountsBalance(Consumer<List<AccountBalance>> consumer, Consumer<String> failed) {
        if (isLoggedIn()) {
            executorService.execute(() -> {
                if (updateAccounts()) {
                    synchronized (metrics) {
                        List<AccountBalance> accountBalance = accountNames
                                .entrySet().stream()
                                .filter(entry -> metrics.containsKey(entry.getKey()))
                                .map(entry -> new AccountBalance(
                                        entry.getKey(),
                                        entry.getValue(),
                                        metrics.getOrDefault(entry.getKey(), new AtomicDouble(0))
                                               .doubleValue()
                                ))
                                .collect(Collectors.toList());
                        consumer.accept(accountBalance);
                    }
                }
                // Failed
                else {
                    failed.accept("Failed to update accounts");
                }
            });
        } else {
            failed.accept("Not logged in");
        }
    }

    private void registerMetrics(AccountList accountList, TransactionAccount account) {
        String id = DigestUtils.md5Hex(account.getFullyFormattedNumber());

        synchronized (metrics) {
            AtomicDouble balance = metrics.get(id);

            // Update
            if (balance != null) {
                balance.set(account.getBalance());
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
                        Tag.of("type", getAccountType(accountList, account)));

                balance = registry.gauge("economy_account", tags, new AtomicDouble(account.getBalance()));
                metrics.put(id, balance);
            }

            accountNames.put(id, account.getName());
        }
    }

    private String getAccountType(AccountList accountList, TransactionAccount account) {
        // Transaction
        if (contains(accountList.getTransactionAccounts(), account)) {
            return "transaction";
        }
        // Loan
        else if (contains(accountList.getLoanAccounts(), account)) {
            return "loan";
        }
        // Saving
        else if (contains(accountList.getSavingAccounts(), account)) {
            return "saving";
        }
        // Transaction disposal
        else if (contains(accountList.getTransactionDisposalAccounts(), account)) {
            return "transaction_disposal";
        }
        // Card
        else if (contains(accountList.getCardAccounts(), account)) {
            return "card";
        }
        // Unknown
        else {
            return "unknown";
        }
    }

    private boolean contains(List<TransactionAccount> accounts, TransactionAccount account) {
        return accounts.stream().anyMatch(a -> a.getId().equals(account.getId()));
    }

    public interface Task {
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean isAborted();

        void abort();
    }

    public class LoginTask implements Task {
        private boolean aborted = false;

        @Override
        public boolean isAborted() {
            return aborted;
        }

        @Override
        public void abort() {
            this.aborted = true;
        }
    }

}
