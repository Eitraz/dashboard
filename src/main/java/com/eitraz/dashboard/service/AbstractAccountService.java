package com.eitraz.dashboard.service;

import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractAccountService {
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MeterRegistry registry;

    // id -> balance (also act as lock for metrics and accountNames)
    private final Map<String, AtomicDouble> metrics = new HashMap<>();

    // id -> name
    private final Map<String, String> accountNames = new HashMap<>();

    void registerMetrics(AccountDetails account) {
        String id = DigestUtils.md5Hex(account.getId());

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
                        Tag.of("type", account.getType()));

                balance = registry.gauge("economy_account", tags, new AtomicDouble(account.getBalance()));
                metrics.put(id, balance);
            }

            accountNames.put(id, account.getName());
        }

        persist(account);
    }

    public List<AccountBalance> getAccountsBalance() {
        synchronized (metrics) {
            return accountNames
                    .entrySet().stream()
                    .filter(entry -> metrics.containsKey(entry.getKey()))
                    .map(entry -> new AccountBalance(
                            entry.getKey(),
                            entry.getValue(),
                            metrics.getOrDefault(entry.getKey(), new AtomicDouble(0))
                                   .doubleValue()
                    ))
                    .collect(Collectors.toList());
        }
    }

    public void persist(AccountDetails account) {
        // TODO: Persist
    }
}
