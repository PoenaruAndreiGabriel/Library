package com.unibuc.library.controller;

import com.unibuc.library.model.Author;
import com.unibuc.library.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/authors")
@Tag(name = "Author Management", description = "APIs for managing authors in the library system")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(
            summary = "Create a new author",
            description = "Creates a new author entry in the library system. Requires author name."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Author created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid author data provided"
            )
    })
    @PostMapping
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author) {
        Author savedAuthor = authorService.createAuthor(author);
        return new ResponseEntity<>(savedAuthor, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all authors",
            description = "Retrieves a list of all authors in the library system with their details and associated books."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all authors"
    )
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @Operation(
            summary = "Get author by ID",
            description = "Retrieves a specific author by their unique identifier, including their books."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Author found and returned"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found with the provided ID"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @Operation(
            summary = "Update an author",
            description = "Updates an existing author by ID. Requires author name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found with the provided ID"),
            @ApiResponse(responseCode = "409", description = "Author with this name already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @Valid @RequestBody Author author) {
        return ResponseEntity.ok(authorService.updateAuthor(id, author));
    }

    @Operation(
            summary = "Delete an author",
            description = "Deletes an author by ID if the author is not linked to any books."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found with the provided ID"),
            @ApiResponse(responseCode = "409", description = "Author cannot be deleted because it is in use")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
