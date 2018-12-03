package com.eitraz.dashboard.service;

import com.github.eitraz.swedbank.SwedbankApi;
import com.github.eitraz.swedbank.exception.SwedbankApiException;
import com.github.eitraz.swedbank.exception.SwedbankClientException;
import com.github.eitraz.swedbank.model.engagement.Overview;
import com.github.eitraz.swedbank.model.engagement.TransactionAccount;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SwedbankService extends AbstractAccountService {
    private static final Logger logger = LoggerFactory.getLogger(SwedbankService.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private LocalDateTime lastUpdateAccountsTime = LocalDateTime.now().minusDays(1);

    private SwedbankApi swedbank = null;

    @PostConstruct
    public void init() {
        // Schedule update
        scheduledExecutorService.scheduleWithFixedDelay(this::updateAccounts, 5, 10, TimeUnit.MINUTES);
    }

    public void updateAccounts() {
        if (!isLoggedIn()) {
            logger.debug("Not logged in, won't update accounts");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // No need to fetch to often
        if (now.isBefore(lastUpdateAccountsTime.plusMinutes(5))) {
            logger.info("No need to fetch accounts");
            return;
        }

        logger.info("Fetching overview");

        try {
            Overview overview = swedbank.getOverview();

            // Accounts
            List<TransactionAccount> accounts = new ArrayList<>();
            accounts.addAll(overview.getTransactionAccounts());
            accounts.addAll(overview.getLoanAccounts());
            accounts.addAll(overview.getSavingAccounts());
            accounts.addAll(overview.getTransactionDisposalAccounts());

            logger.info("Accounts fetched, registering metrics");

            // Get account details and register metrics
            accounts.stream()
                    .map(transactionAccount -> getAccountDetails(overview, transactionAccount))
                    .forEach(this::registerMetrics);

            lastUpdateAccountsTime = LocalDateTime.now();
        } catch (Throwable e) {
            logger.error("Error while fetching accounts", e);
            swedbank = null;
        }
    }

    private AccountDetails getAccountDetails(Overview overview, TransactionAccount transactionAccount) {
        try {
            // Fetch details
            if (transactionAccount.getDetails() != null) {
                com.github.eitraz.swedbank.model.engagement.account.AccountDetails details = swedbank.getAccountDetails(transactionAccount);

                return new AccountDetails()
                        .setId(details.getFullyFormattedNumber())
                        .setName(Optional.ofNullable(StringUtils.trimToNull(details.getName().getCurrent()))
                                         .orElse(transactionAccount.getName()))
                        .setType(getAccountType(overview, transactionAccount))
                        .setBalance(details.getAvailableAmount().getAmount());
            }
            // Get details from overview
            else {
                return new AccountDetails()
                        .setId(transactionAccount.getFullyFormattedNumber())
                        .setName(transactionAccount.getName())
                        .setType(getAccountType(overview, transactionAccount))
                        .setBalance(transactionAccount.getBalance());
            }

        } catch (SwedbankClientException | SwedbankApiException e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLoggedIn() {
        return swedbank != null;
    }

    public void login(SwedbankApi swedbankApi) {
        this.swedbank = swedbankApi;
    }

    private String getAccountType(Overview accountList, TransactionAccount account) {
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

}
