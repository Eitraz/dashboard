package com.eitraz.dashboard.service;

import com.github.eitraz.swedbank.SwedbankApi;
import com.github.eitraz.swedbank.model.engagement.Overview;
import com.github.eitraz.swedbank.model.engagement.TransactionAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SwedbankService extends AbstractAccountService {
    private static final Logger logger = LoggerFactory.getLogger(SwedbankService.class);

    private LocalDateTime lastUpdateAccountsTime = LocalDateTime.now().minusDays(1);

    public boolean updateAccounts(SwedbankApi swedbank) {
        logger.info("Update accounts");

        try {
            LocalDateTime now = LocalDateTime.now();
            // No need to fetch to often
            if (now.isBefore(lastUpdateAccountsTime.plusMinutes(5))) {
                logger.info("No need to fetch accounts");
                return true;
            }

            logger.info("Fetching overview");

            Overview overview = swedbank.getOverview();

            // Accounts
            List<TransactionAccount> accounts = new ArrayList<>();
            accounts.addAll(overview.getTransactionAccounts());
            accounts.addAll(overview.getLoanAccounts());
            accounts.addAll(overview.getSavingAccounts());
            accounts.addAll(overview.getTransactionDisposalAccounts());

            logger.info("Accounts fetched, registering metrics");

            // Register metrics
            accounts.forEach(account -> registerMetrics(accountToAccountDetails(overview, account)));

            lastUpdateAccountsTime = LocalDateTime.now();
            return true;
        } catch (Throwable e) {
            logger.error("Error while fetching accounts", e);
            return false;
        }
    }

    private AccountDetails accountToAccountDetails(Overview overview, TransactionAccount account) {
        return new AccountDetails()
                .setId(account.getFullyFormattedNumber())
                .setName(account.getName())
                .setType(getAccountType(overview, account))
                .setBalance(account.getBalance());
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
