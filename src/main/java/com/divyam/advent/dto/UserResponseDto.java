package com.divyam.advent.dto;

import com.divyam.advent.enums.Culture;

public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Culture country;

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String name, String email, Culture country) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.country = country;
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
}
