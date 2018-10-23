package com.github.soshibby.swedbank.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Override com.github.soshibby.swedbank.types.PrivateProfile from com.github.soshibby:swedbank:1.2.1
 * There are new fields added, ignore those
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivateProfile {

    private String id;
    private String activeProfileLanguage;
    private String bankId;
    private String customerNumber;
    private String bankName;
    private String customerName;
    private Links links;
    private Boolean customerInternational;
    private Boolean youthProfile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActiveProfileLanguage() {
        return activeProfileLanguage;
    }

    public void setActiveProfileLanguage(String activeProfileLanguage) {
        this.activeProfileLanguage = activeProfileLanguage;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Boolean getCustomerInternational() {
        return customerInternational;
    }

    public void setCustomerInternational(Boolean customerInternational) {
        this.customerInternational = customerInternational;
    }

    public Boolean getYouthProfile() {
        return youthProfile;
    }

    public void setYouthProfile(Boolean youthProfile) {
        this.youthProfile = youthProfile;
    }

}
