package com.eitraz.dashboard.service.ica.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    @JsonAlias("FirstName")
    private String firstName;

    @JsonAlias("LastName")
    private String lastName;

    @JsonAlias("Ttl")
    private Long ttl;

    @JsonAlias("CustomerRole")
    private Long customerRole;

    @JsonAlias("Id")
    private Double id;

    @JsonAlias("ZipCode")
    private String zipCode;

    @JsonAlias("City")
    private String city;

    @JsonAlias("Gender")
    private String gender;

    @JsonAlias("YearOfBirth")
    private String yearOfBirth;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Long getCustomerRole() {
        return customerRole;
    }

    public void setCustomerRole(Long customerRole) {
        this.customerRole = customerRole;
    }

    public Double getId() {
        return id;
    }

    public void setId(Double id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
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
