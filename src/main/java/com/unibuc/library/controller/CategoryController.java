package com.unibuc.library.controller;

import com.unibuc.library.model.Book;
import com.unibuc.library.model.Category;
import com.unibuc.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/categories")
@Tag(name = "Category Management", description = "APIs for managing book categories in the library system")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "Create a new category",
            description = "Creates a new book category in the library system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category with this name already exists"
            )
    })
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category savedCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all categories",
            description = "Retrieves a list of all categories in the library system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all categories"
    )
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(
            summary = "Get category by ID",
            description = "Retrieves a specific category by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category found and returned"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found with the provided ID"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(
            summary = "Update a category",
            description = "Updates an existing category by ID. Requires category name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found with the provided ID"),
            @ApiResponse(responseCode = "409", description = "Category with this name already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes a category by ID if it is not linked to any books."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found with the provided ID"),
            @ApiResponse(responseCode = "409", description = "Category cannot be deleted because it is in use")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Search books by category name",
            description = "Search for books by category name (partial match). Returns all books that belong to categories matching the search term."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Category name search term cannot be empty"
            )
    })
    @GetMapping("/search/books")
    public ResponseEntity<List<Book>> getBooksByCategoryName(
            @Parameter(
                    required = true,
                    example = "fantasy"
            )
            @RequestParam String categoryName
    ) {
        return ResponseEntity.ok(categoryService.getBooksByCategoryName(categoryName));
    }
}
