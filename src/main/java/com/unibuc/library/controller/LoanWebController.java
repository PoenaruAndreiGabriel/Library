package com.unibuc.library.controller;

import com.unibuc.library.model.Loan;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.UserRepository;
import com.unibuc.library.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/loans")
public class LoanWebController {

    private final LoanService loanService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public LoanWebController(LoanService loanService,
                             UserRepository userRepository,
                             BookRepository bookRepository) {
        this.loanService = loanService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String listLoans(@RequestParam(defaultValue = "active") String filter, Model model) {
        List<Loan> loans = switch (filter) {
            case "overdue" -> loanService.getOverdueLoans();
            case "all"    -> loanService.getAllLoans();
            default       -> loanService.getActiveLoans();
        };
        model.addAttribute("loans", loans);
        model.addAttribute("filter", filter);
        return "loans/list";
    }

    @GetMapping("/new")
    public String showBorrowForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("books", bookRepository.findAllWithAuthorsAndCategory());
        return "loans/form";
    }

    @PostMapping("/new")
    public String borrowBook(@RequestParam Long userId,
                             @RequestParam Long bookId,
                             RedirectAttributes redirectAttributes) {
        try {
            loanService.borrowBook(userId, bookId);
            redirectAttributes.addFlashAttribute("successMessage", "Book borrowed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            loanService.returnBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/loans";
    }
}
