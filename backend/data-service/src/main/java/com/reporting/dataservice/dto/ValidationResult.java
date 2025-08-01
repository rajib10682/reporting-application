package com.reporting.dataservice.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private List<String> errors;
    
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }
    
    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
