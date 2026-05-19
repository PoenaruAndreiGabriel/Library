package com.unibuc.library.controller;

import com.unibuc.library.model.Loan;
import com.unibuc.library.model.ReservationStatus;
import com.unibuc.library.repository.*;
import com.unibuc.library.service.LoanService;
import com.unibuc.library.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LoanService loanService;
    private final ReservationService reservationService;

    public DashboardController(BookRepository bookRepository,
                               AuthorRepository authorRepository,
                               CategoryRepository categoryRepository,
                               UserRepository userRepository,
                               LoanService loanService,
                               ReservationService reservationService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.loanService = loanService;
        this.reservationService = reservationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Stats
        model.addAttribute("totalBooks",       bookRepository.count());
        model.addAttribute("totalAuthors",     authorRepository.count());
        model.addAttribute("totalCategories",  categoryRepository.count());
        model.addAttribute("totalUsers",       userRepository.count());

        List<Loan> activeLoans  = loanService.getActiveLoans();
        List<Loan> overdueLoans = loanService.getOverdueLoans();

        model.addAttribute("activeLoansCount",  activeLoans.size());
        model.addAttribute("overdueLoansCount", overdueLoans.size());

        long activeReservations = reservationService.getAllReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .count();
        model.addAttribute("activeReservationsCount", activeReservations);

        // Recent active loans (max 5)
        List<Loan> recentLoans = activeLoans.stream()
                .sorted((a, b) -> b.getBorrowDate().compareTo(a.getBorrowDate()))
                .limit(5)
                .toList();
        model.addAttribute("recentLoans", recentLoans);

        // Overdue loans for alert
        model.addAttribute("overdueLoans", overdueLoans);

        return "dashboard";
    }
}
