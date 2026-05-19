package com.unibuc.library.controller;

import com.unibuc.library.dto.UserForm;
import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserProfile;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserWebController {

    private final UserService userService;
    private final int pageSize;

    public UserWebController(UserService userService,
                             @Value("${library.pagination.page-size:5}") int pageSize) {
        this.userService = userService;
        this.pageSize = pageSize;
    }

    @GetMapping
    public String listUsers(@RequestParam(required = false) String name,
                            @RequestParam(required = false) String role,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "name") String sort,
                            @RequestParam(defaultValue = "asc") String dir,
                            Model model) {
        boolean searching = (name != null && !name.isBlank()) || (role != null && !role.isBlank());
        Page<User> usersPage;

        if (searching) {
            List<User> users = userService.getAllUsers();
            if (name != null && !name.isBlank()) {
                users = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
            }
            if (role != null && !role.isBlank()) {
                try {
                    UserRole r = UserRole.valueOf(role.toUpperCase());
                    users = users.stream().filter(u -> u.getRole() == r).toList();
                } catch (IllegalArgumentException ignored) {}
            }
            usersPage = new PageImpl<>(users);
        } else {
            Pageable pageable = PageRequest.of(
                    Math.max(page, 0),
                    pageSize,
                    Sort.by(resolveDirection(dir), resolveUserSort(sort))
            );
            usersPage = userService.getUsersPage(pageable);
        }

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("searchName", name);
        model.addAttribute("searchRole", role);
        model.addAttribute("searching", searching);
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("pageSize", pageSize);
        return "users/list";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "users/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("pageTitle", "Add New User");
        return "users/form";
    }

    @PostMapping("/new")
    public String createUser(@Valid @ModelAttribute("userForm") UserForm form,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("pageTitle", "Add New User");
            return "users/form";
        }
        try {
            userService.createUser(buildUser(form, null));
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/users";
        } catch (DuplicateResourceException e) {
            result.rejectValue("email", "email.duplicate", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("pageTitle", "Add New User");
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("userForm", userToForm(userService.getUserById(id)));
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("pageTitle", "Edit User");
        return "users/form";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("userForm") UserForm form,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("pageTitle", "Edit User");
            return "users/form";
        }
        try {
            userService.updateUser(id, buildUser(form, id));
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/users";
        } catch (DuplicateResourceException e) {
            result.rejectValue("email", "email.duplicate", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("pageTitle", "Edit User");
            return "users/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (ResourceInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private User buildUser(UserForm form, Long existingId) {
        User user = new User();
        if (existingId != null) user.setId(existingId);
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        user.setMaxBorrowLimit(form.getMaxBorrowLimit());
        user.setPassword(form.getPassword());

        if ((form.getPhoneNumber() != null && !form.getPhoneNumber().isBlank())
                || (form.getAddress() != null && !form.getAddress().isBlank())) {
            UserProfile profile = new UserProfile();
            profile.setPhoneNumber(form.getPhoneNumber() != null ? form.getPhoneNumber() : "");
            profile.setAddress(form.getAddress() != null ? form.getAddress() : "");
            user.setProfile(profile);
        }
        return user;
    }

    private UserForm userToForm(User user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole());
        form.setMaxBorrowLimit(user.getMaxBorrowLimit());
        if (user.getProfile() != null) {
            form.setPhoneNumber(user.getProfile().getPhoneNumber());
            form.setAddress(user.getProfile().getAddress());
        }
        return form;
    }

    private String resolveUserSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "email" -> "email";
            case "role" -> "role";
            default -> "name";
        };
    }

    private Sort.Direction resolveDirection(String dir) {
        return "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}
