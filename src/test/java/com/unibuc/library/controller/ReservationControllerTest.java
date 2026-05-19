package com.unibuc.library.controller;

import com.unibuc.library.model.*;
import com.unibuc.library.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

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
        when(reservationService.createReservation(1L, 1L)).thenReturn(reservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.createReservation(1L, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(user, response.getBody().getUser());
        assertEquals(book, response.getBody().getBook());
        assertEquals(ReservationStatus.ACTIVE, response.getBody().getStatus());
        verify(reservationService).createReservation(1L, 1L);
    }

    @Test
    void createReservation_DifferentUserAndBook_Success() {
        // Arrange
        when(reservationService.createReservation(2L, 3L)).thenReturn(reservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.createReservation(2L, 3L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reservationService).createReservation(2L, 3L);
    }

    @Test
    void getUserReservations_Success() {
        // Arrange
        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setUser(user);
        reservation2.setBook(book);
        reservation2.setStatus(ReservationStatus.COMPLETED);

        List<Reservation> reservations = Arrays.asList(reservation, reservation2);
        when(reservationService.getReservationsForUser(1L)).thenReturn(reservations);

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getUserReservations(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(2L, response.getBody().get(1).getId());
        verify(reservationService).getReservationsForUser(1L);
    }

    @Test
    void getUserReservations_EmptyList_ReturnsOkWithEmptyList() {
        // Arrange
        when(reservationService.getReservationsForUser(1L)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getUserReservations(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(reservationService).getReservationsForUser(1L);
    }

    @Test
    void getUserReservations_DifferentUser_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setUser(user2);

        when(reservationService.getReservationsForUser(2L)).thenReturn(Arrays.asList(reservation2));

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getUserReservations(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(reservationService).getReservationsForUser(2L);
    }

    @Test
    void cancelReservation_Success() {
        // Arrange
        doNothing().when(reservationService).cancelReservation(1L);

        // Act
        ResponseEntity<Void> response = reservationController.cancelReservation(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(reservationService).cancelReservation(1L);
    }

    @Test
    void cancelReservation_DifferentReservation_Success() {
        // Arrange
        doNothing().when(reservationService).cancelReservation(5L);

        // Act
        ResponseEntity<Void> response = reservationController.cancelReservation(5L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reservationService).cancelReservation(5L);
    }

    @Test
    void completeReservation_Success() {
        // Arrange
        reservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationService.completeReservation(1L)).thenReturn(reservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.completeReservation(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(ReservationStatus.COMPLETED, response.getBody().getStatus());
        verify(reservationService).completeReservation(1L);
    }

    @Test
    void completeReservation_DifferentReservation_Success() {
        // Arrange
        when(reservationService.completeReservation(3L)).thenReturn(reservation);

        // Act
        ResponseEntity<Reservation> response = reservationController.completeReservation(3L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reservationService).completeReservation(3L);
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

        List<Reservation> queue = Arrays.asList(reservation2, reservation);
        when(reservationService.getReservationQueueForBook(1L)).thenReturn(queue);

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getReservationQueueForBook(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getId()); // Older reservation first
        assertEquals(1L, response.getBody().get(1).getId());
        verify(reservationService).getReservationQueueForBook(1L);
    }

    @Test
    void getReservationQueueForBook_EmptyQueue_ReturnsEmptyList() {
        // Arrange
        when(reservationService.getReservationQueueForBook(1L)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getReservationQueueForBook(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(reservationService).getReservationQueueForBook(1L);
    }

    @Test
    void getReservationQueueForBook_DifferentBook_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setBook(book2);

        when(reservationService.getReservationQueueForBook(2L)).thenReturn(Arrays.asList(reservation2));

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getReservationQueueForBook(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(reservationService).getReservationQueueForBook(2L);
    }

    @Test
    void createReservation_ServiceInvoked_Once() {
        // Arrange
        when(reservationService.createReservation(1L, 1L)).thenReturn(reservation);

        // Act
        reservationController.createReservation(1L, 1L);

        // Assert
        verify(reservationService, times(1)).createReservation(1L, 1L);
    }

    @Test
    void getUserReservations_ServiceInvoked_Once() {
        // Arrange
        when(reservationService.getReservationsForUser(1L)).thenReturn(Arrays.asList(reservation));

        // Act
        reservationController.getUserReservations(1L);

        // Assert
        verify(reservationService, times(1)).getReservationsForUser(1L);
    }

    @Test
    void cancelReservation_ServiceInvoked_Once() {
        // Arrange
        doNothing().when(reservationService).cancelReservation(1L);

        // Act
        reservationController.cancelReservation(1L);

        // Assert
        verify(reservationService, times(1)).cancelReservation(1L);
    }

    @Test
    void completeReservation_ServiceInvoked_Once() {
        // Arrange
        when(reservationService.completeReservation(1L)).thenReturn(reservation);

        // Act
        reservationController.completeReservation(1L);

        // Assert
        verify(reservationService, times(1)).completeReservation(1L);
    }

    @Test
    void getReservationQueueForBook_ServiceInvoked_Once() {
        // Arrange
        when(reservationService.getReservationQueueForBook(1L)).thenReturn(Arrays.asList(reservation));

        // Act
        reservationController.getReservationQueueForBook(1L);

        // Assert
        verify(reservationService, times(1)).getReservationQueueForBook(1L);
    }

    @Test
    void getUserReservations_MultipleStatuses_Success() {
        // Arrange
        Reservation activeReservation = new Reservation();
        activeReservation.setId(1L);
        activeReservation.setStatus(ReservationStatus.ACTIVE);

        Reservation completedReservation = new Reservation();
        completedReservation.setId(2L);
        completedReservation.setStatus(ReservationStatus.COMPLETED);

        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(3L);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);

        List<Reservation> reservations = Arrays.asList(
                activeReservation, completedReservation, cancelledReservation
        );
        when(reservationService.getReservationsForUser(1L)).thenReturn(reservations);

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getUserReservations(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals(1, response.getBody().stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .count());
        assertEquals(1, response.getBody().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .count());
        assertEquals(1, response.getBody().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED)
                .count());
        verify(reservationService).getReservationsForUser(1L);
    }

    @Test
    void getReservationQueueForBook_MultipleReservations_OrderedCorrectly() {
        // Arrange
        Reservation oldest = new Reservation();
        oldest.setId(1L);
        oldest.setReservationDate(LocalDate.now().minusDays(5));

        Reservation middle = new Reservation();
        middle.setId(2L);
        middle.setReservationDate(LocalDate.now().minusDays(3));

        Reservation newest = new Reservation();
        newest.setId(3L);
        newest.setReservationDate(LocalDate.now());

        List<Reservation> queue = Arrays.asList(oldest, middle, newest);
        when(reservationService.getReservationQueueForBook(1L)).thenReturn(queue);

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getReservationQueueForBook(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals(3L, response.getBody().get(2).getId());
        verify(reservationService).getReservationQueueForBook(1L);
    }
}