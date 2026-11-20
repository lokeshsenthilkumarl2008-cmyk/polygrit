
package com.polyglotmesh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExecutionRequest {

    @NotBlank(message = "Language cannot be empty")
    private String language;

    @NotBlank(message = "Code cannot be empty")
    private String code;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
