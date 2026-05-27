package com.divyam.advent.dto;

import com.divyam.advent.enums.Culture;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Culture country;
    private boolean isAdmin;
    private boolean isSuperAdmin;

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String name, String email, Culture country) {
        this(id, name, email, country, false, false);
    }

    public UserResponseDto(Long id, String name, String email, Culture country,
                           boolean isAdmin, boolean isSuperAdmin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.country = country;
        this.isAdmin = isAdmin;
        this.isSuperAdmin = isSuperAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Culture getCountry() {
        return country;
    }

    public void setCountry(Culture country) {
        this.country = country;
    }

    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    @JsonProperty("isSuperAdmin")
    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.isSuperAdmin = superAdmin;
    }
}
