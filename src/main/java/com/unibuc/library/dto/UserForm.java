package com.unibuc.library.dto;

import com.unibuc.library.model.UserRole;
import jakarta.validation.constraints.*;

/**
 * Form-backing DTO for User create / edit — includes optional profile fields.
 */
public class UserForm {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Borrow limit is required")
    @Min(value = 1, message = "Borrow limit must be at least 1")
    private Integer maxBorrowLimit;

    // Optional profile fields
    private String phoneNumber;
    private String address;
    
    // Optional password field (when creating or updating password)
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public Integer getMaxBorrowLimit() { return maxBorrowLimit; }
    public void setMaxBorrowLimit(Integer maxBorrowLimit) { this.maxBorrowLimit = maxBorrowLimit; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
