package com.divyam.advent.dto;

import com.divyam.advent.enums.Culture;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthEnsureUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private Culture country = Culture.GLOBAL;

    public AuthEnsureUserRequest() {
    }

    public AuthEnsureUserRequest(String name, String email, Culture country) {
        this.name = name;
        this.email = email;
        this.country = country;
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
}
