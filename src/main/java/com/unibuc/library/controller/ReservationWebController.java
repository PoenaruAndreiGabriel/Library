package com.unibuc.library.controller;

import com.unibuc.library.model.Reservation;
import com.unibuc.library.model.ReservationStatus;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.UserRepository;
import com.unibuc.library.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationWebController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReservationWebController(ReservationService reservationService,
                                    UserRepository userRepository,
                                    BookRepository bookRepository) {
        this.reservationService = reservationService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String listReservations(@RequestParam(defaultValue = "active") String filter, Model model) {
        List<Reservation> reservations = reservationService.getAllReservations().stream()
                .filter(r -> switch (filter) {
                    case "all"       -> true;
                    case "cancelled" -> r.getStatus() == ReservationStatus.CANCELLED;
                    case "completed" -> r.getStatus() == ReservationStatus.COMPLETED;
                    default          -> r.getStatus() == ReservationStatus.ACTIVE;
                })
                .toList();
        model.addAttribute("reservations", reservations);
        model.addAttribute("filter", filter);
        return "reservations/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("books", bookRepository.findAllWithAuthorsAndCategory());
        return "reservations/form";
    }

    @PostMapping("/new")
    public String createReservation(@RequestParam Long userId,
                                    @RequestParam Long bookId,
                                    RedirectAttributes redirectAttributes) {
        try {
            reservationService.createReservation(userId, bookId);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/complete")
    public String completeReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.completeReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation completed and converted to loan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reservations";
    }
}
