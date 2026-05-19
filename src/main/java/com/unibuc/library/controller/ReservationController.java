package com.unibuc.library.controller;

import com.unibuc.library.model.Reservation;
import com.unibuc.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/reservations")
@Tag(name = "Reservation Management", description = "APIs for managing book reservations in the library system")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(
            summary = "Create a reservation",
            description = "Creates a reservation for a book. User can reserve a book even if no copies are currently available."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation created successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or book not found"
            )
    })
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestParam Long userId,
            @RequestParam Long bookId
    ) {
        Reservation reservation = reservationService.createReservation(userId, bookId);
        return ResponseEntity.ok(reservation);
    }

    @Operation(
            summary = "Get user's reservations",
            description = "Retrieves all reservations for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User's reservations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getUserReservations(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(reservationService.getReservationsForUser(userId));
    }

    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels an existing reservation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Reservation cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "ID of the reservation to cancel", required = true, example = "1")
            @PathVariable Long id
    ) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Complete a reservation (convert to loan)",
            description = "When a reserved book becomes available, completes the first active reservation by converting it to a loan. Automatically notifies the next user in line."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation completed and converted to loan"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<Reservation> completeReservation(
            @Parameter(description = "ID of the reservation to complete", required = true, example = "1")
            @PathVariable Long id
    ) {
        Reservation completedReservation = reservationService.completeReservation(id);
        return ResponseEntity.ok(completedReservation);
    }

    @Operation(
            summary = "Get reservation queue for a book",
            description = "Retrieves all active reservations for a specific book, ordered by reservation date (first come, first served)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation queue retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found"
            )
    })
    @GetMapping("/book/{bookId}/queue")
    public ResponseEntity<List<Reservation>> getReservationQueueForBook(
            @Parameter(description = "ID of the book", required = true, example = "1")
            @PathVariable Long bookId
    ) {
        return ResponseEntity.ok(reservationService.getReservationQueueForBook(bookId));
    }
}
