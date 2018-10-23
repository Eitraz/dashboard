package com.github.soshibby.swedbank.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Override com.github.soshibby.swedbank.types.Bank from com.github.soshibby:swedbank:1.2.1
 * There are new fields added, ignore those
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bank {

    private String bankId;
    private String name;
    private PrivateProfile privateProfile;
    private List<CorporateProfile> corporateProfiles;

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrivateProfile getPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(PrivateProfile privateProfile) {
        this.privateProfile = privateProfile;
    }

    public List<CorporateProfile> getCorporateProfiles() {
        return corporateProfiles;
    }

    public void setCorporateProfiles(List<CorporateProfile> corporateProfiles) {
        this.corporateProfiles = corporateProfiles;
    }
}
