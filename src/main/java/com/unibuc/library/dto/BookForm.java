package com.unibuc.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * Form-backing DTO for Book create / edit operations.
 * Authors are submitted as a comma-separated string and split server-side.
 */
public class BookForm {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^[\\d\\-]{10,17}$", message = "ISBN must be 10–17 digits/hyphens")
    private String isbn;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    @NotNull(message = "Category is required")
    private Long categoryId;

    /** Comma-separated author names, e.g. "George Orwell, Aldous Huxley" */
    @NotBlank(message = "At least one author is required")
    private String authorsInput;

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getAuthorsInput() { return authorsInput; }
    public void setAuthorsInput(String authorsInput) { this.authorsInput = authorsInput; }

    /** Splits authorsInput by comma and trims each name. */
    public List<String> getAuthorNames() {
        if (authorsInput == null || authorsInput.isBlank()) return List.of();
        return java.util.Arrays.stream(authorsInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
