package com.unibuc.library.controller;


import com.unibuc.library.model.Loan;
import com.unibuc.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/loans")
@Tag(name = "Loan Management", description = "APIs for managing book loans in the library system")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(
            summary = "Borrow a book",
            description = "Allows a user to borrow a book. Checks availability and user's borrow limit."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book borrowed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or book not found"
            )
    })
    @PostMapping("/borrow")
    public ResponseEntity<Loan> borrowBook(
            @RequestParam Long userId,
            @RequestParam Long bookId
    ) {
        Loan loan = loanService.borrowBook(userId, bookId);
        return ResponseEntity.ok(loan);
    }

    @Operation(
            summary = "Return a book",
            description = "Returns a borrowed book and updates its availability."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Book already returned"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"
            )
    })
    @PostMapping("/return/{loanId}")
    public ResponseEntity<Loan> returnBook(@PathVariable Long loanId) {
        Loan loan = loanService.returnBook(loanId);
        return ResponseEntity.ok(loan);
    }

    @Operation(
            summary = "Get active loans",
            description = "Retrieves all currently active (not returned) loans."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active loans retrieved successfully"
    )
    @GetMapping("/active")
    public ResponseEntity<List<Loan>> getActiveLoans() {
        return ResponseEntity.ok(loanService.getActiveLoans());
    }

    @Operation(
            summary = "Get user's active loans",
            description = "Retrieves all currently active loans for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User's active loans retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Loan>> getActiveLoansByUser(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(loanService.getActiveLoansByUser(userId));
    }

    @Operation(
            summary = "Get user's loan history",
            description = "Retrieves all loans (active and returned) for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User's loan history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Loan>> getLoanHistoryByUser(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(loanService.getLoanHistoryByUser(userId));
    }

    @Operation(
            summary = "Get overdue loans",
            description = "Retrieves all loans that are overdue (not returned and past due date)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Overdue loans retrieved successfully"
    )
    @GetMapping("/overdue")
    public ResponseEntity<List<Loan>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.getOverdueLoans());
    }
}
