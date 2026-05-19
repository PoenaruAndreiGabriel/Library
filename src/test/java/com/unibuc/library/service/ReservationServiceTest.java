package com.unibuc.library.service;

import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.*;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.ReservationRepository;
import com.unibuc.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Book book;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(5);

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        book.setTotalCopies(10);
        book.setAvailableCopies(0);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus(ReservationStatus.ACTIVE);
    }

    @Test
    void createReservation_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.createReservation(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(ReservationStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getReservationDate());
        verify(userRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(999L, 1L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_BookNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(1L, 999L));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository).findById(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_UserAlreadyHasActiveReservation_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(1L, 1L));

        assertEquals("User already has active reservation for this book", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_UserHasCancelledReservation_Success() {
        // Arrange
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(2L);
        cancelledReservation.setUser(user);
        cancelledReservation.setBook(book);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(cancelledReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.createReservation(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.ACTIVE, result.getStatus());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void getReservationsForUser_Success() {
        // Arrange
        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setUser(user);
        reservation2.setBook(book);
        reservation2.setStatus(ReservationStatus.COMPLETED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation, reservation2));

        // Act
        List<Reservation> result = reservationService.getReservationsForUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getUser().getId() == 1L));
        verify(userRepository).findById(1L);
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationsForUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservationsForUser(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getReservationsForUser_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Reservation> result = reservationService.getReservationsForUser(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reservationRepository).findAll();
    }

    @Test
    void cancelReservation_Success() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        reservationService.cancelReservation(1L);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository).findById(1L);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void cancelReservation_ReservationNotFound_ThrowsException() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.cancelReservation(999L));

        assertEquals("Reservation not found with id: 999", exception.getMessage());
        verify(reservationRepository).findById(999L);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_AlreadyCancelled_ThrowsException() {
        // Arrange
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(1L));

        assertEquals("Cannot cancel a reservation that is not active", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_AlreadyCompleted_ThrowsException() {
        // Arrange
        reservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(1L));

        assertEquals("Cannot cancel a reservation that is not active", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void completeReservation_Success() {
        // Arrange
        book.setAvailableCopies(1);
        Loan loan = new Loan();
        loan.setId(1L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(loanService.borrowBook(1L, 1L)).thenReturn(loan);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.completeReservation(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ReservationStatus.COMPLETED, result.getStatus());
        verify(reservationRepository).findById(1L);
        verify(loanService).borrowBook(1L, 1L);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void completeReservation_ReservationNotFound_ThrowsException() {
        // Arrange
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.completeReservation(999L));

        assertEquals("Reservation not found with id: 999", exception.getMessage());
        verify(reservationRepository).findById(999L);
        verify(loanService, never()).borrowBook(any(), any());
    }

    @Test
    void completeReservation_NotActive_ThrowsException() {
        // Arrange
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.completeReservation(1L));

        assertEquals("Cannot complete a reservation that is not active", exception.getMessage());
        verify(loanService, never()).borrowBook(any(), any());
    }

    @Test
    void completeReservation_NoAvailableCopies_ThrowsException() {
        // Arrange
        book.setAvailableCopies(0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.completeReservation(1L));

        assertEquals("No available copies to fulfill this reservation", exception.getMessage());
        verify(loanService, never()).borrowBook(any(), any());
    }

    @Test
    void completeReservation_LoanServiceFails_ThrowsException() {
        // Arrange
        book.setAvailableCopies(1);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(loanService.borrowBook(1L, 1L)).thenThrow(new RuntimeException("Borrow limit reached"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> reservationService.completeReservation(1L));

        assertTrue(exception.getMessage().contains("Failed to convert reservation to loan"));
        verify(loanService).borrowBook(1L, 1L);
        verify(reservationRepository, never()).save(reservation);
    }

    @Test
    void getReservationQueueForBook_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setUser(user2);
        reservation2.setBook(book);
        reservation2.setReservationDate(LocalDate.now().minusDays(1));
        reservation2.setStatus(ReservationStatus.ACTIVE);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation, reservation2));

        // Act
        List<Reservation> result = reservationService.getReservationQueueForBook(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId()); // Older reservation first
        assertEquals(1L, result.get(1).getId());
        verify(bookRepository).findById(1L);
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationQueueForBook_BookNotFound_ThrowsException() {
        // Arrange
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservationQueueForBook(999L));

        assertEquals("Book not found with id: 999", exception.getMessage());
        verify(bookRepository).findById(999L);
    }

    @Test
    void getReservationQueueForBook_OnlyActiveReservations_Success() {
        // Arrange
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(2L);
        cancelledReservation.setUser(user);
        cancelledReservation.setBook(book);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);

        Reservation completedReservation = new Reservation();
        completedReservation.setId(3L);
        completedReservation.setUser(user);
        completedReservation.setBook(book);
        completedReservation.setStatus(ReservationStatus.COMPLETED);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(
                Arrays.asList(reservation, cancelledReservation, completedReservation)
        );

        // Act
        List<Reservation> result = reservationService.getReservationQueueForBook(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ReservationStatus.ACTIVE, result.get(0).getStatus());
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationQueueForBook_EmptyQueue_ReturnsEmptyList() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Reservation> result = reservationService.getReservationQueueForBook(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationQueueForBook_SortedByDate_Success() {
        // Arrange
        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setBook(book);
        reservation2.setReservationDate(LocalDate.now().minusDays(5));
        reservation2.setStatus(ReservationStatus.ACTIVE);

        Reservation reservation3 = new Reservation();
        reservation3.setId(3L);
        reservation3.setBook(book);
        reservation3.setReservationDate(LocalDate.now().minusDays(2));
        reservation3.setStatus(ReservationStatus.ACTIVE);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reservationRepository.findAll()).thenReturn(
                Arrays.asList(reservation, reservation3, reservation2)
        );

        // Act
        List<Reservation> result = reservationService.getReservationQueueForBook(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(2L, result.get(0).getId()); // Oldest first (5 days ago)
        assertEquals(3L, result.get(1).getId()); // Middle (2 days ago)
        assertEquals(1L, result.get(2).getId()); // Newest (today)
        verify(reservationRepository).findAll();
    }
}