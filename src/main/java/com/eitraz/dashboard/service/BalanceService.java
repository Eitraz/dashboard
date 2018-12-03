package com.eitraz.dashboard.service;

import com.eitraz.dashboard.domain.Balance;
import com.eitraz.dashboard.domain.BalanceRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.LocalDateTime.now;
import static org.springframework.data.domain.Example.of;
import static org.springframework.data.domain.Sort.Order.desc;

@Service
public class BalanceService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceService.class);

    private final BalanceRepository balanceRepository;

    private final Map<String, Double> balanceCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> timestampCache = new ConcurrentHashMap<>();

    @Autowired
    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    private Optional<Double> getLastBalance(String id) {
        if (balanceCache.get(id) != null) {
            return Optional.ofNullable(balanceCache.get(id));
        }

        return getLastBalanceFromDatabase(id);
    }

    private Optional<LocalDateTime> lastTimestamp(String id) {
        return Optional.ofNullable(timestampCache.get(id));
    }

    private Optional<Double> getLastBalanceFromDatabase(String id) {
        // Get last persisted balance
        List<Balance> balanceList = balanceRepository.findAll(
                of(new Balance().setAccountId(id)),
                PageRequest.of(0, 1, Sort.by(desc("timestamp")))
        ).getContent();

        for (Balance balance : balanceList) {
            logger.debug(balance.getAccountId() + ", " + balance.getName() + ": " + balance.getBalance().doubleValue());
        }

        if (!balanceList.isEmpty()) {
            return Optional.of(balanceList.get(0).getBalance().doubleValue());
        }

        return Optional.empty();
    }

    void persist(AccountDetails accountDetails, Duration persistInterval) {
        String id = DigestUtils.md5Hex(accountDetails.getId());
        Double balance = accountDetails.getBalance();

        // Don't persist to often
        if (persistInterval != null) {
            Optional<LocalDateTime> lastTime = lastTimestamp(id);
            if (lastTime.isPresent() && now().isBefore(lastTime.get().plus(persistInterval))) {
                logger.debug(String.format("Won't persist %s balance %s, to soon", accountDetails.getName(), balance));
                return;
            }
        }

        Optional<Double> lastBalance = getLastBalance(id);

        // No need to persist
        if (lastBalance.isPresent() && Objects.equals(balance, lastBalance.get())) {
            logger.debug(String.format("No need to persist %s balance %s", accountDetails.getName(), balance));

            // Prevent extra database calls by caching value here as well
            balanceCache.put(id, balance);
            return;
        }

        // Persist
        balanceRepository.save(new Balance().setAccountId(id)
                                            .setName(accountDetails.getName())
                                            .setBalance(BigDecimal.valueOf(balance)));

        // Store in balanceCache
        balanceCache.put(id, balance);
        timestampCache.put(id, now());
    }
}
