package com.unibuc.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Handles exceptions only for MVC (Thymeleaf) web controllers.
 * REST controllers under /rest/** are excluded — they keep their own
 * @ResponseStatus annotations and return JSON automatically.
 */
@ControllerAdvice(basePackages = "com.unibuc.library.controller",
        assignableTypes = {
                com.unibuc.library.controller.BookWebController.class,
                com.unibuc.library.controller.AuthorWebController.class,
                com.unibuc.library.controller.CategoryWebController.class,
                com.unibuc.library.controller.UserWebController.class,
                com.unibuc.library.controller.LoanWebController.class,
                com.unibuc.library.controller.ReservationWebController.class,
                com.unibuc.library.controller.DashboardController.class,
                com.unibuc.library.controller.HomeController.class
        })
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 404);
        return "error/404";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateResourceException ex, Model model) {
        model.addAttribute("errorTitle", "Duplicate Resource");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error/error";
    }

    @ExceptionHandler(ResourceInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInUse(ResourceInUseException ex, Model model) {
        model.addAttribute("errorTitle", "Resource In Use");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "Something went wrong on our end. Please try again later.");
        model.addAttribute("statusCode", 500);
        return "error/500";
    }
}
