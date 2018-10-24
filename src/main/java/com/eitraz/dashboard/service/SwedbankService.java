package com.eitraz.dashboard.service;

import com.github.soshibby.swedbank.Swedbank;
import com.github.soshibby.swedbank.app.SwedbankApp;
import com.github.soshibby.swedbank.authentication.MobileBankID;
import com.github.soshibby.swedbank.types.AccountList;
import com.github.soshibby.swedbank.types.TransactionAccount;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class SwedbankService {
    private static final Logger logger = LoggerFactory.getLogger(SwedbankService.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<RegistryEntry> registryCache = new ArrayList<>();

    @Autowired
    public SwedbankService(MeterRegistry registry) {
        // Update metrics at a scheduled interval
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            synchronized (registryCache) {
                registryCache.forEach(entry -> registry.gauge(entry.getName(), entry.getTags(), entry.getValue()));
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public Task getAccounts(String personalNumber, Consumer<List<TransactionAccount>> listener) {
        Swedbank swedbank = new Swedbank();
        MobileBankID mobileBankID = new MobileBankID(new SwedbankApp(), personalNumber);

        Task task = new GetAccountsTask();

        executorService.execute(() -> {
            try {
                swedbank.login(mobileBankID);

                while (!task.isAborted() && !swedbank.isLoggedIn()) {
                    Thread.sleep(5000);
                }

                if (!task.isAborted()) {
                    AccountList accountList = swedbank.accountList();
                    List<TransactionAccount> accounts = accountList.getAllAccounts();

                    // Register metrics
                    List<RegistryEntry> newRegistryCache = accounts
                            .stream()
                            .map(account -> {
                                String tag = Normalizer
                                        .normalize(account.getName(), Normalizer.Form.NFKD)
                                        .replaceAll("\\p{M}", "")
                                        .replace(" ", "_")
                                        .replace("-", "_")
                                        .toLowerCase();

                                String id = DigestUtils.md5Hex(account.getFullyFormattedNumber());

                                return new RegistryEntry("economy_account",
                                        Arrays.asList(
                                                Tag.of("id", id),
                                                Tag.of("tag", tag),
                                                Tag.of("name", account.getName()),
                                                Tag.of("type", getAccountType(accountList, account))),
                                        account.getBalance());
                            })
                            .collect(Collectors.toList());

                    // Update metrics registry cache
                    synchronized (registryCache) {
                        registryCache.clear();
                        registryCache.addAll(newRegistryCache);
                    }

                    listener.accept(accounts);

                    return;
                }
            } catch (Throwable e) {
                logger.error("Error while fetching accounts", e);
            }

            task.abort();
            listener.accept(null);
        });

        return task;
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

    public class GetAccountsTask implements Task {
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

    private class RegistryEntry {
        private final String name;
        private final List<Tag> tags;
        private final Double value;

        RegistryEntry(String name, List<Tag> tags, Double value) {
            this.name = name;
            this.tags = tags;
            this.value = value;
        }

        String getName() {
            return name;
        }

        List<Tag> getTags() {
            return tags;
        }

        Double getValue() {
            return value;
        }
    }
}
