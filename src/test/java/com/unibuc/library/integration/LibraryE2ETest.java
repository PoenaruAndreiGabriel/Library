package com.unibuc.library.integration;

import com.unibuc.library.model.*;
import com.unibuc.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LibraryE2ETest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void cleanup() {
        // clear DB between tests
        reservationRepository.deleteAll();
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void e2e_borrowBook_flow() {
        // create user
        User user = new User();
        user.setName("Borrower");
        user.setEmail("borrower@example.com");
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(2);
        user = userRepository.save(user);

        // create book
        Book book = new Book();
        book.setTitle("E2E Book");
        book.setIsbn("E2E-ISBN-1");
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        book = bookRepository.save(book);

        // call borrow endpoint
        String path = "/rest/loans/borrow?userId=" + user.getId() + "&bookId=" + book.getId();
        ResponseEntity<Loan> response = restTemplate.postForEntity(url(path), null, Loan.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Loan loan = response.getBody();
        assertNotNull(loan);
        assertEquals(user.getId(), loan.getUser().getId());
        assertEquals(book.getId(), loan.getBook().getId());

        // verify available copies decreased
        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(2, updated.getAvailableCopies());
    }

    @Test
    void e2e_reservation_complete_flow() {
        // create two users
        User u1 = new User();
        u1.setName("First");
        u1.setEmail("first@example.com");
        u1.setRole(UserRole.MEMBER);
        u1.setMaxBorrowLimit(2);
        u1 = userRepository.save(u1);

        User u2 = new User();
        u2.setName("Second");
        u2.setEmail("second@example.com");
        u2.setRole(UserRole.MEMBER);
        u2.setMaxBorrowLimit(2);
        u2 = userRepository.save(u2);

        // create book with 1 copy
        Book book = new Book();
        book.setTitle("Reservable Book");
        book.setIsbn("E2E-ISBN-2");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        book = bookRepository.save(book);

        // u1 borrows the book
        String borrowPath = "/rest/loans/borrow?userId=" + u1.getId() + "&bookId=" + book.getId();
        ResponseEntity<Loan> borrowResp = restTemplate.postForEntity(url(borrowPath), null, Loan.class);
        assertEquals(HttpStatus.OK, borrowResp.getStatusCode());

        // u2 creates reservation
        String reservePath = "/rest/reservations?userId=" + u2.getId() + "&bookId=" + book.getId();
        ResponseEntity<Reservation> reserveResp = restTemplate.postForEntity(url(reservePath), null, Reservation.class);
        assertEquals(HttpStatus.OK, reserveResp.getStatusCode());
        Reservation reservation = reserveResp.getBody();
        assertNotNull(reservation);

        // u1 returns the book by calling return endpoint
        Loan loan = borrowResp.getBody();
        assertNotNull(loan);
        String returnPath = "/rest/loans/return/" + loan.getId();
        ResponseEntity<Loan> returnResp = restTemplate.postForEntity(url(returnPath), null, Loan.class);
        assertEquals(HttpStatus.OK, returnResp.getStatusCode());

        // now complete the reservation -> should create a loan for u2 and mark reservation completed
        String completePath = "/rest/reservations/" + reservation.getId() + "/complete";
        ResponseEntity<Reservation> completeResp = restTemplate.postForEntity(url(completePath), null, Reservation.class);
        assertEquals(HttpStatus.OK, completeResp.getStatusCode());
        Reservation completed = completeResp.getBody();
        assertNotNull(completed);
        assertEquals(ReservationStatus.COMPLETED, completed.getStatus());

        // available copies should be 0 again (loan created)
        Book updated = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(0, updated.getAvailableCopies());
    }

    @Test
    void e2e_overdue_detection_flow() {
        // create user and book
        User user = new User();
        user.setName("OverdueUser");
        user.setEmail("overdue@example.com");
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(2);
        user = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Old Loan Book");
        book.setIsbn("E2E-ISBN-3");
        book.setTotalCopies(1);
        book.setAvailableCopies(0);
        book = bookRepository.save(book);

        // insert a loan with dueDate in the past
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setBorrowDate(LocalDate.now().minusDays(30));
        loan.setDueDate(LocalDate.now().minusDays(15));
        loanRepository.save(loan);

        // call overdue endpoint
        String overduePath = "/rest/loans/overdue";
        ResponseEntity<Loan[]> resp = restTemplate.getForEntity(url(overduePath), Loan[].class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Loan[] loans = resp.getBody();
        assertNotNull(loans);
        assertTrue(loans.length >= 1, "Expected at least one overdue loan");
    }
}



