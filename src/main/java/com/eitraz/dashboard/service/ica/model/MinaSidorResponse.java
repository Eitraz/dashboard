package com.eitraz.dashboard.service.ica.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MinaSidorResponse {
    @JsonAlias("YearlyTotalPurchased")
    private Double yearlyTotalPurchased;

    @JsonAlias("AcquiredDiscount")
    private Double acquiredDiscount;

    @JsonAlias("AmountSinceLastBonusCheck")
    private Double amountSinceLastBonusCheck;

    @JsonAlias("AmountLeftUntilNextBonusCheck")
    private Double amountLeftUntilNextBonusCheck;

    @JsonAlias("AcquiredBonus")
    private Double acquiredBonus;

    @JsonAlias("IcaBankUrl")
    private String icaBankUrl;

    @JsonAlias("BonusToDate")
    private String bonusToDate;

    @JsonAlias("Accounts")
    private List<Account> accounts;

    public Double getYearlyTotalPurchased() {
        return yearlyTotalPurchased;
    }

    public void setYearlyTotalPurchased(Double yearlyTotalPurchased) {
        this.yearlyTotalPurchased = yearlyTotalPurchased;
    }

    public Double getAcquiredDiscount() {
        return acquiredDiscount;
    }

    public void setAcquiredDiscount(Double acquiredDiscount) {
        this.acquiredDiscount = acquiredDiscount;
    }

    public Double getAmountSinceLastBonusCheck() {
        return amountSinceLastBonusCheck;
    }

    public void setAmountSinceLastBonusCheck(Double amountSinceLastBonusCheck) {
        this.amountSinceLastBonusCheck = amountSinceLastBonusCheck;
    }

    public Double getAmountLeftUntilNextBonusCheck() {
        return amountLeftUntilNextBonusCheck;
    }

    public void setAmountLeftUntilNextBonusCheck(Double amountLeftUntilNextBonusCheck) {
        this.amountLeftUntilNextBonusCheck = amountLeftUntilNextBonusCheck;
    }

    public Double getAcquiredBonus() {
        return acquiredBonus;
    }

    public void setAcquiredBonus(Double acquiredBonus) {
        this.acquiredBonus = acquiredBonus;
    }

    public String getIcaBankUrl() {
        return icaBankUrl;
    }

    public void setIcaBankUrl(String icaBankUrl) {
        this.icaBankUrl = icaBankUrl;
    }

    public String getBonusToDate() {
        return bonusToDate;
    }

    public void setBonusToDate(String bonusToDate) {
        this.bonusToDate = bonusToDate;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
