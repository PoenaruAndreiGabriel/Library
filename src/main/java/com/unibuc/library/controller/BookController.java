package com.unibuc.library.controller;


import com.unibuc.library.model.Book;
import com.unibuc.library.service.BookService;
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
@RequestMapping("/rest/books")
@Tag(name = "Book Management", description = "APIs for managing books in the library system")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
            summary = "Create a new book",
            description = "Creates a new book entry in the library system. Requires book details including title, ISBN, author, category, and copy information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Book with this ISBN already exists"
            )
    })
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        Book savedBook = bookService.createBook(book);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all books",
            description = "Retrieves a list of all books in the library system with their full details."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all books"
    )
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @Operation(
            summary = "Get book by ID",
            description = "Retrieves a specific book by its unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book found and returned"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found with the provided ID"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(
            summary = "Delete a book",
            description = "Removes a book from the library system by its ID. This action is irreversible."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Book successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found with the provided ID"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook((id));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Search books by title keyword",
            description = "Search for books containing a specific keyword in their title (partial match)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully"
            )
    })
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam String keyword) {
        return ResponseEntity.ok(bookService.searchBooksByTitle(keyword));
    }

    @Operation(
            summary = "Advanced book search",
            description = "Search books using multiple criteria (title, author name, and/or category name). All searches are partial matches. You can use any combination of parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully"
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(bookService.searchBooks(title, author, category));
    }
}
