package com.unibuc.library.controller;

import com.unibuc.library.dto.BookForm;
import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Author;
import com.unibuc.library.model.Book;
import com.unibuc.library.model.Category;
import com.unibuc.library.repository.AuthorRepository;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.CategoryRepository;
import com.unibuc.library.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MVC controller for the Book UI (Thymeleaf views).
 * All REST endpoints remain under /rest/books.
 */
@Controller
@RequestMapping("/books")
public class BookWebController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final int defaultPageSize;

    public BookWebController(BookService bookService,
                             BookRepository bookRepository,
                             CategoryRepository categoryRepository,
                              AuthorRepository authorRepository,
                              @Value("${library.pagination.page-size:5}") int defaultPageSize) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.defaultPageSize = defaultPageSize;
    }

    // ── LIST ──────────────────────────────────────────────────────────────

    @GetMapping
    public String listBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) Integer pageSize,
            Model model) {

        boolean searching = (title != null && !title.isBlank())
                || (author != null && !author.isBlank())
                || (category != null && !category.isBlank());
        Page booksPage;

        if (searching) {
            List<Book> books = bookService.searchBooks(
                    blankToNull(title),
                    blankToNull(author),
                    blankToNull(category));
            booksPage = new PageImpl<>(books);
        } else {
            Pageable pageable = PageRequest.of(
                    Math.max(page, 0),
                    resolvePageSize(pageSize),
                    Sort.by(resolveDirection(dir), resolveBookSort(sort))
            );
            booksPage = bookService.getBooksPage(pageable);
        }

        model.addAttribute("booksPage", booksPage);
        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("searchTitle", title);
        model.addAttribute("searchAuthor", author);
        model.addAttribute("searchCategory", category);
        model.addAttribute("searching", searching);
        model.addAttribute("currentPage", booksPage.getNumber());
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("pageSize", resolvePageSize(pageSize));
        return "books/list";
    }

    // ── DETAIL ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "books/detail";
    }

    // ── CREATE ────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("pageTitle", "Add New Book");
        return "books/form";
    }

    @PostMapping("/new")
    @Transactional
    public String createBook(@Valid @ModelAttribute("bookForm") BookForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (form.getAvailableCopies() != null && form.getTotalCopies() != null
                && form.getAvailableCopies() > form.getTotalCopies()) {
            result.rejectValue("availableCopies", "copies.invalid",
                    "Available copies cannot exceed total copies");
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("pageTitle", "Add New Book");
            return "books/form";
        }

        try {
            saveBookFromForm(form, null);
            redirectAttributes.addFlashAttribute("successMessage", "Book created successfully!");
            return "redirect:/books";
        } catch (DuplicateResourceException e) {
            result.rejectValue("isbn", "isbn.duplicate", e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("pageTitle", "Add New Book");
            return "books/form";
        }
    }

    // ── EDIT ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        BookForm form = bookToForm(book);
        model.addAttribute("bookForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("pageTitle", "Edit Book");
        return "books/form";
    }

    @PostMapping("/{id}/edit")
    @Transactional
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute("bookForm") BookForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (form.getAvailableCopies() != null && form.getTotalCopies() != null
                && form.getAvailableCopies() > form.getTotalCopies()) {
            result.rejectValue("availableCopies", "copies.invalid",
                    "Available copies cannot exceed total copies");
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("pageTitle", "Edit Book");
            return "books/form";
        }

        try {
            saveBookFromForm(form, id);
            redirectAttributes.addFlashAttribute("successMessage", "Book updated successfully!");
            return "redirect:/books";
        } catch (DuplicateResourceException e) {
            result.rejectValue("isbn", "isbn.duplicate", e.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("pageTitle", "Edit Book");
            return "books/form";
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully.");
        } catch (ResourceInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books";
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Creates or updates a book within the current transaction.
     * By loading/saving the managed entity inside the same transaction,
     * JPA correctly writes the book_authors join table.
     */
    @Transactional
    protected void saveBookFromForm(BookForm form, Long existingId) {
        Book book;
        if (existingId != null) {
            // Load the managed entity so Hibernate tracks changes
            book = bookRepository.findById(existingId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Book not found with id: " + existingId));
        } else {
            // Check ISBN uniqueness
            bookRepository.findByIsbn(form.getIsbn()).ifPresent(b -> {
                throw new DuplicateResourceException(
                        "A book with ISBN '" + form.getIsbn() + "' already exists");
            });
            book = new Book();
        }

        book.setTitle(form.getTitle());
        book.setIsbn(form.getIsbn());
        book.setTotalCopies(form.getTotalCopies());
        book.setAvailableCopies(form.getAvailableCopies());

        // Resolve category (managed entity)
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + form.getCategoryId()));
        book.setCategory(category);

        // Resolve authors — find existing by name or create new managed entities
        Set<Author> authors = new HashSet<>();
        for (String name : form.getAuthorNames()) {
            Author author = authorRepository.findByName(name)
                    .orElseGet(() -> authorRepository.save(new Author(name)));
            authors.add(author);
        }
        book.setAuthors(authors);

        // saveAndFlush ensures the INSERT + join table rows are written in this transaction
        bookRepository.saveAndFlush(book);
    }

    private BookForm bookToForm(Book book) {
        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setTitle(book.getTitle());
        form.setIsbn(book.getIsbn());
        form.setTotalCopies(book.getTotalCopies());
        form.setAvailableCopies(book.getAvailableCopies());
        if (book.getCategory() != null) {
            form.setCategoryId(book.getCategory().getId());
        }
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            String authorsStr = book.getAuthors().stream()
                    .map(Author::getName)
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            form.setAuthorsInput(authorsStr);
        }
        return form;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private String resolveBookSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "isbn" -> "isbn";
            case "copies" -> "totalCopies";
            default -> "title";
        };
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
