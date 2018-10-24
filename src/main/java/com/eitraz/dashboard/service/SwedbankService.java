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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class SwedbankService {
    private static final Logger logger = LoggerFactory.getLogger(SwedbankService.class);

    private final MeterRegistry registry;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    public SwedbankService(MeterRegistry registry) {
        this.registry = registry;
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
                    accounts.forEach(account -> {
                        String tag = Normalizer
                                .normalize(account.getName(), Normalizer.Form.NFKD)
                                .replaceAll("\\p{M}", "")
                                .replace(" ", "_")
                                .replace("-", "_")
                                .toLowerCase();

                        String id = DigestUtils.md5Hex(account.getFullyFormattedNumber());

                        registry.gauge(
                                "economy_account",
                                Arrays.asList(
                                        Tag.of("id", id),
                                        Tag.of("tag", tag),
                                        Tag.of("name", account.getName()),
                                        Tag.of("type", getAccountType(accountList, account))),
                                account.getBalance());
                    });

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
}
