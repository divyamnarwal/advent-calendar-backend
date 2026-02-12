package com.divyam.advent.dto;

import com.divyam.advent.enums.ThemePreference;
import jakarta.validation.constraints.NotNull;

public class ThemePreferenceUpdateRequestDto {

    @NotNull(message = "themePreference is required")
    private ThemePreference themePreference;

    public ThemePreferenceUpdateRequestDto() {
    }

    public ThemePreferenceUpdateRequestDto(ThemePreference themePreference) {
        this.themePreference = themePreference;
    }

    public ThemePreference getThemePreference() {
        return themePreference;
    }

    public void setThemePreference(ThemePreference themePreference) {
        this.themePreference = themePreference;
    }
}
