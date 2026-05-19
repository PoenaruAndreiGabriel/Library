package com.unibuc.library.controller;

import com.unibuc.library.model.Book;
import com.unibuc.library.model.Loan;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.service.LoanService;
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
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    private User user;
    private Book book;
    private Loan loan;

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
        book.setAvailableCopies(10);

        loan = new Loan();
        loan.setId(1L);
        loan.setUser(user);
        loan.setBook(book);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
    }

    @Test
    void borrowBook_Success() {
        // Arrange
        when(loanService.borrowBook(1L, 1L)).thenReturn(loan);

        // Act
        ResponseEntity<Loan> response = loanController.borrowBook(1L, 1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(user, response.getBody().getUser());
        assertEquals(book, response.getBody().getBook());
        assertNotNull(response.getBody().getBorrowDate());
        assertNotNull(response.getBody().getDueDate());
        verify(loanService).borrowBook(1L, 1L);
    }

    @Test
    void borrowBook_DifferentUserAndBook_Success() {
        // Arrange
        when(loanService.borrowBook(2L, 3L)).thenReturn(loan);

        // Act
        ResponseEntity<Loan> response = loanController.borrowBook(2L, 3L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(loanService).borrowBook(2L, 3L);
    }

    @Test
    void returnBook_Success() {
        // Arrange
        loan.setReturnDate(LocalDate.now());
        when(loanService.returnBook(1L)).thenReturn(loan);

        // Act
        ResponseEntity<Loan> response = loanController.returnBook(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertNotNull(response.getBody().getReturnDate());
        verify(loanService).returnBook(1L);
    }

    @Test
    void returnBook_ServiceInvoked_Once() {
        // Arrange
        when(loanService.returnBook(1L)).thenReturn(loan);

        // Act
        loanController.returnBook(1L);

        // Assert
        verify(loanService, times(1)).returnBook(1L);
    }

    @Test
    void getActiveLoans_Success() {
        // Arrange
        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setUser(user);
        loan2.setBook(book);

        List<Loan> activeLoans = Arrays.asList(loan, loan2);
        when(loanService.getActiveLoans()).thenReturn(activeLoans);

        // Act
        ResponseEntity<List<Loan>> response = loanController.getActiveLoans();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(loanService).getActiveLoans();
    }

    @Test
    void getActiveLoans_EmptyList_ReturnsOkWithEmptyList() {
        // Arrange
        when(loanService.getActiveLoans()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Loan>> response = loanController.getActiveLoans();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(loanService).getActiveLoans();
    }

    @Test
    void getActiveLoansByUser_Success() {
        // Arrange
        when(loanService.getActiveLoansByUser(1L)).thenReturn(Arrays.asList(loan));

        // Act
        ResponseEntity<List<Loan>> response = loanController.getActiveLoansByUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getUser().getId());
        verify(loanService).getActiveLoansByUser(1L);
    }

    @Test
    void getActiveLoansByUser_DifferentUser_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);

        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setUser(user2);
        loan2.setBook(book);

        when(loanService.getActiveLoansByUser(2L)).thenReturn(Arrays.asList(loan2));

        // Act
        ResponseEntity<List<Loan>> response = loanController.getActiveLoansByUser(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getUser().getId());
        verify(loanService).getActiveLoansByUser(2L);
    }

    @Test
    void getLoanHistoryByUser_Success() {
        // Arrange
        Loan returnedLoan = new Loan();
        returnedLoan.setId(2L);
        returnedLoan.setUser(user);
        returnedLoan.setBook(book);
        returnedLoan.setReturnDate(LocalDate.now().minusDays(5));

        List<Loan> loanHistory = Arrays.asList(loan, returnedLoan);
        when(loanService.getLoanHistoryByUser(1L)).thenReturn(loanHistory);

        // Act
        ResponseEntity<List<Loan>> response = loanController.getLoanHistoryByUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(loanService).getLoanHistoryByUser(1L);
    }

    @Test
    void getLoanHistoryByUser_EmptyHistory_ReturnsEmptyList() {
        // Arrange
        when(loanService.getLoanHistoryByUser(1L)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Loan>> response = loanController.getLoanHistoryByUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(loanService).getLoanHistoryByUser(1L);
    }

    @Test
    void getOverdueLoans_Success() {
        // Arrange
        Loan overdueLoan = new Loan();
        overdueLoan.setId(2L);
        overdueLoan.setUser(user);
        overdueLoan.setBook(book);
        overdueLoan.setBorrowDate(LocalDate.now().minusDays(20));
        overdueLoan.setDueDate(LocalDate.now().minusDays(6));

        when(loanService.getOverdueLoans()).thenReturn(Arrays.asList(overdueLoan));

        // Act
        ResponseEntity<List<Loan>> response = loanController.getOverdueLoans();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getDueDate().isBefore(LocalDate.now()));
        verify(loanService).getOverdueLoans();
    }

    @Test
    void getOverdueLoans_NoOverdueLoans_ReturnsEmptyList() {
        // Arrange
        when(loanService.getOverdueLoans()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Loan>> response = loanController.getOverdueLoans();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(loanService).getOverdueLoans();
    }

    @Test
    void borrowBook_ServiceInvoked_Once() {
        // Arrange
        when(loanService.borrowBook(1L, 1L)).thenReturn(loan);

        // Act
        loanController.borrowBook(1L, 1L);

        // Assert
        verify(loanService, times(1)).borrowBook(1L, 1L);
    }

    @Test
    void getActiveLoans_ServiceInvoked_Once() {
        // Arrange
        when(loanService.getActiveLoans()).thenReturn(Arrays.asList(loan));

        // Act
        loanController.getActiveLoans();

        // Assert
        verify(loanService, times(1)).getActiveLoans();
    }

    @Test
    void getActiveLoansByUser_ServiceInvoked_Once() {
        // Arrange
        when(loanService.getActiveLoansByUser(1L)).thenReturn(Arrays.asList(loan));

        // Act
        loanController.getActiveLoansByUser(1L);

        // Assert
        verify(loanService, times(1)).getActiveLoansByUser(1L);
    }

    @Test
    void getLoanHistoryByUser_ServiceInvoked_Once() {
        // Arrange
        when(loanService.getLoanHistoryByUser(1L)).thenReturn(Arrays.asList(loan));

        // Act
        loanController.getLoanHistoryByUser(1L);

        // Assert
        verify(loanService, times(1)).getLoanHistoryByUser(1L);
    }

    @Test
    void getOverdueLoans_ServiceInvoked_Once() {
        // Arrange
        when(loanService.getOverdueLoans()).thenReturn(Arrays.asList());

        // Act
        loanController.getOverdueLoans();

        // Assert
        verify(loanService, times(1)).getOverdueLoans();
    }

    @Test
    void getActiveLoansByUser_MultipleLoans_Success() {
        // Arrange
        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setUser(user);

        Loan loan3 = new Loan();
        loan3.setId(3L);
        loan3.setUser(user);

        List<Loan> userLoans = Arrays.asList(loan, loan2, loan3);
        when(loanService.getActiveLoansByUser(1L)).thenReturn(userLoans);

        // Act
        ResponseEntity<List<Loan>> response = loanController.getActiveLoansByUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(l -> l.getUser().getId() == 1L));
        verify(loanService).getActiveLoansByUser(1L);
    }
}