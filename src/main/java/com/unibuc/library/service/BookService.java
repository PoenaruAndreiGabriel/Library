package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Book;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    public Book createBook(Book book) {
        log.info("Creating book with ISBN '{}' and title '{}'", book.getIsbn(), book.getTitle());
        // Check if ISBN already exists
        bookRepository.findByIsbn(book.getIsbn())
                .ifPresent(existingBook -> {
                    throw new DuplicateResourceException(
                            "A book with ISBN '" + book.getIsbn() + "' already exists"
                    );
                });

        return bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        log.debug("Fetching all books with authors and category");
        return bookRepository.findAllWithAuthorsAndCategory();
    }

    public Page<Book> getBooksPage(Pageable pageable) {
        log.debug("Fetching books page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable);
    }

    public Book getBookById(Long id) {
        log.debug("Fetching book by id {}", id);
        return bookRepository.findByIdWithAuthorsAndCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id
                ));
    }

    public void deleteBook(Long id) {
        Book book = getBookById(id);
        if (loanRepository.existsByBookId(id)) {
            log.warn("Cannot delete book '{}' (id={}) because it has associated loans", book.getTitle(), id);
            throw new ResourceInUseException(
                "Book \"" + book.getTitle() + "\" cannot be deleted because it has associated loans."
            );
        }
        log.info("Deleting book '{}' (id={})", book.getTitle(), id);
        bookRepository.delete(book);
    }

    public List<Book> searchBooksByTitle(String keyword) {
        log.debug("Searching books by title with keyword '{}'", keyword);
        return bookRepository.findAllWithAuthorsAndCategory().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooks(String title, String author, String category) {
        log.debug("Searching books with filters title='{}', author='{}', category='{}'", title, author, category);
        return bookRepository.findAllWithAuthorsAndCategory().stream()
                .filter(book -> title == null ||
                        (book.getTitle().toLowerCase().contains(title.toLowerCase())))
                .filter(book -> author == null ||
                        (book.getAuthors() != null &&
                                book.getAuthors().stream()
                                        .anyMatch(a -> a.getName().toLowerCase().contains(author.toLowerCase()))))
                .filter(book -> category == null ||
                        (book.getCategory() != null &&
                                book.getCategory().getName().toLowerCase().contains(category.toLowerCase())))
                .collect(Collectors.toList());
    }
}
