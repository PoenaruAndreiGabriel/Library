package com.unibuc.library.controller;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.model.Author;
import com.unibuc.library.service.AuthorService;
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
@RequestMapping("/authors")
public class AuthorWebController {

    private final AuthorService authorService;
    private final int defaultPageSize;

    public AuthorWebController(AuthorService authorService,
                               @Value("${library.pagination.page-size:5}") int defaultPageSize) {
        this.authorService = authorService;
        this.defaultPageSize = defaultPageSize;
    }

    // ── LIST ──────────────────────────────────────────────────────────────

    @GetMapping
    public String listAuthors(@RequestParam(required = false) String name,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "name") String sort,
                              @RequestParam(defaultValue = "asc") String dir,
                              @RequestParam(required = false) Integer pageSize,
                              Model model) {
        boolean searching = name != null && !name.isBlank();
        Page authorsPage;

        if (searching) {
            List<Author> authors = authorService.getAllAuthors().stream()
                    .filter(a -> a.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
            authorsPage = new PageImpl<>(authors);
        } else {
            Pageable pageable = PageRequest.of(
                    Math.max(page, 0),
                    resolvePageSize(pageSize),
                    Sort.by(resolveDirection(dir), resolveAuthorSort(sort))
            );
            authorsPage = authorService.getAuthorsPage(pageable);
        }

        model.addAttribute("authorsPage", authorsPage);
        model.addAttribute("authors", authorsPage.getContent());
        model.addAttribute("searchName", name);
        model.addAttribute("searching", searching);
        model.addAttribute("currentPage", authorsPage.getNumber());
        model.addAttribute("totalPages", authorsPage.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("pageSize", resolvePageSize(pageSize));
        return "authors/list";
    }

    // ── DETAIL ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String viewAuthor(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getAuthorById(id));
        return "authors/detail";
    }

    // ── CREATE ────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new Author());
        model.addAttribute("pageTitle", "Add New Author");
        return "authors/form";
    }

    @PostMapping("/new")
    public String createAuthor(@Valid @ModelAttribute("author") Author author,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Add New Author");
            return "authors/form";
        }
        try {
            authorService.createAuthor(author);
            redirectAttributes.addFlashAttribute("successMessage", "Author created successfully!");
            return "redirect:/authors";
        } catch (DuplicateResourceException e) {
            result.rejectValue("name", "name.duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Add New Author");
            return "authors/form";
        }
    }

    // ── EDIT ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.getAuthorById(id));
        model.addAttribute("pageTitle", "Edit Author");
        return "authors/form";
    }

    @PostMapping("/{id}/edit")
    public String updateAuthor(@PathVariable Long id,
                               @Valid @ModelAttribute("author") Author author,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Author");
            return "authors/form";
        }
        try {
            authorService.updateAuthor(id, author);
            redirectAttributes.addFlashAttribute("successMessage", "Author updated successfully!");
            return "redirect:/authors";
        } catch (DuplicateResourceException e) {
            result.rejectValue("name", "name.duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Edit Author");
            return "authors/form";
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Author deleted successfully.");
        } catch (ResourceInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/authors";
    }

    private String resolveAuthorSort(String sort) {
        if ("id".equalsIgnoreCase(sort)) {
            return "id";
        }
        return "name";
    }

    private Sort.Direction resolveDirection(String dir) {
        return "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private int resolvePageSize(Integer pageSize) {
        if (pageSize == null) {
            return defaultPageSize;
        }
        if (pageSize == 5 || pageSize == 10 || pageSize == 20) {
            return pageSize;
        }
        return defaultPageSize;
    }
}
