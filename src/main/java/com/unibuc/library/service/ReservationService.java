package com.unibuc.library.service;

import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.*;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.ReservationRepository;
import com.unibuc.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LoanService loanService;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              BookRepository bookRepository,
                              LoanService loanService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.loanService = loanService;
    }

    public Reservation createReservation(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        boolean hasActiveReservation = reservationRepository.findAll().stream()
                .anyMatch(reservation ->
                        reservation.getUser().getId() == userId &&
                                reservation.getBook().getId().equals(bookId) &&
                                reservation.getStatus() == ReservationStatus.ACTIVE
                );

        if (hasActiveReservation) {
            throw new RuntimeException("User already has active reservation for this book");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus(ReservationStatus.ACTIVE);

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsForUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getUser().getId() == userId)
                .collect(Collectors.toList());
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel a reservation that is not active");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public Reservation completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new RuntimeException("Cannot complete a reservation that is not active");
        }

        // Check if book is available
        Book book = reservation.getBook();
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No available copies to fulfill this reservation");
        }

        // Convert reservation to loan
        try {
            Loan loan = loanService.borrowBook(
                    reservation.getUser().getId(),
                    reservation.getBook().getId()
            );

            // Mark reservation as completed
            reservation.setStatus(ReservationStatus.COMPLETED);
            return reservationRepository.save(reservation);

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert reservation to loan: " + e.getMessage());
        }
    }

    public List<Reservation> getReservationQueueForBook(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getBook().getId().equals(bookId))
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACTIVE)
                .sorted(Comparator.comparing(Reservation::getReservationDate)) // Oldest first
                .collect(Collectors.toList());
    }

}
