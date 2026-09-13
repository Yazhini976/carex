package com.carex.dto.specialty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SpecialtyRequest {

    @NotBlank(message = "Specialty name is required")
    @Size(min = 2, max = 100, message = "Specialty name must be between 2 and 100 characters")
    private String name;

    private String description;

    public SpecialtyRequest() {}

    public SpecialtyRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
