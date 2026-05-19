package com.unibuc.library.service;

import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Book;
import com.unibuc.library.model.Loan;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.LoanRepository;
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
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LoanService loanService;

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.countByUserIdAndReturnDateIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act
        Loan result = loanService.borrowBook(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(9, book.getAvailableCopies());
        verify(userRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(book);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrowBook_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> loanService.borrowBook(999L, 1L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(bookRepository, never()).findById(any());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_BookNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> loanService.borrowBook(1L, 999L));

        assertEquals("Book not found with id: 999", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(bookRepository).findById(999L);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_NoAvailableCopies_ThrowsException() {
        // Arrange
        book.setAvailableCopies(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> loanService.borrowBook(1L, 1L));

        assertEquals("No available copies for this book", exception.getMessage());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrowBook_UserReachedBorrowLimit_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.countByUserIdAndReturnDateIsNull(1L)).thenReturn(5L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> loanService.borrowBook(1L, 1L));

        assertEquals("User has reached borrow limit of 5 books", exception.getMessage());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnBook_Success() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // Act
        Loan result = loanService.returnBook(1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getReturnDate());
        assertEquals(LocalDate.now(), result.getReturnDate());
        assertEquals(11, book.getAvailableCopies());
        verify(loanRepository).findById(1L);
        verify(bookRepository).save(book);
        verify(loanRepository).save(loan);
    }

    @Test
    void returnBook_LoanNotFound_ThrowsException() {
        // Arrange
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> loanService.returnBook(999L));

        assertEquals("Loan not found with id: 999", exception.getMessage());
        verify(loanRepository).findById(999L);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void returnBook_AlreadyReturned_ThrowsException() {
        // Arrange
        loan.setReturnDate(LocalDate.now().minusDays(1));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> loanService.returnBook(1L));

        assertEquals("Book already returned", exception.getMessage());
        verify(loanRepository).findById(1L);
        verify(bookRepository, never()).save(any());
        verify(loanRepository, never()).save(any());
    }

    @Test
    void getActiveLoans_Success() {
        // Arrange
        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setUser(user);
        loan2.setBook(book);

        List<Loan> activeLoans = Arrays.asList(loan, loan2);
        when(loanRepository.findByReturnDateIsNull()).thenReturn(activeLoans);

        // Act
        List<Loan> result = loanService.getActiveLoans();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loanRepository).findByReturnDateIsNull();
    }

    @Test
    void getActiveLoans_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(loanRepository.findByReturnDateIsNull()).thenReturn(Arrays.asList());

        // Act
        List<Loan> result = loanService.getActiveLoans();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanRepository).findByReturnDateIsNull();
    }

    @Test
    void getActiveLoansByUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loanRepository.findByReturnDateIsNull()).thenReturn(Arrays.asList(loan));

        // Act
        List<Loan> result = loanService.getActiveLoansByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUser().getId());
        verify(userRepository).findById(1L);
        verify(loanRepository).findByReturnDateIsNull();
    }

    @Test
    void getActiveLoansByUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> loanService.getActiveLoansByUser(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getLoanHistoryByUser_Success() {
        // Arrange
        Loan returnedLoan = new Loan();
        returnedLoan.setId(2L);
        returnedLoan.setUser(user);
        returnedLoan.setBook(book);
        returnedLoan.setReturnDate(LocalDate.now().minusDays(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loanRepository.findAll()).thenReturn(Arrays.asList(loan, returnedLoan));

        // Act
        List<Loan> result = loanService.getLoanHistoryByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findById(1L);
        verify(loanRepository).findAll();
    }

    @Test
    void getLoanHistoryByUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> loanService.getLoanHistoryByUser(999L));

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
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

        when(loanRepository.findByReturnDateIsNull()).thenReturn(Arrays.asList(loan, overdueLoan));

        // Act
        List<Loan> result = loanService.getOverdueLoans();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertTrue(result.get(0).getDueDate().isBefore(LocalDate.now()));
        verify(loanRepository).findByReturnDateIsNull();
    }

    @Test
    void getOverdueLoans_NoOverdueLoans_ReturnsEmptyList() {
        // Arrange
        when(loanRepository.findByReturnDateIsNull()).thenReturn(Arrays.asList(loan));

        // Act
        List<Loan> result = loanService.getOverdueLoans();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanRepository).findByReturnDateIsNull();
    }

    @Test
    void borrowBook_DecreasesAvailableCopies() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.countByUserIdAndReturnDateIsNull(1L)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        int initialCopies = book.getAvailableCopies();

        // Act
        loanService.borrowBook(1L, 1L);

        // Assert
        assertEquals(initialCopies - 1, book.getAvailableCopies());
        verify(bookRepository).save(book);
    }

    @Test
    void returnBook_IncreasesAvailableCopies() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        int initialCopies = book.getAvailableCopies();

        // Act
        loanService.returnBook(1L);

        // Assert
        assertEquals(initialCopies + 1, book.getAvailableCopies());
        verify(bookRepository).save(book);
    }
}