package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Author;
import com.unibuc.library.repository.AuthorRepository;
import com.unibuc.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private static final Logger log = LoggerFactory.getLogger(AuthorService.class);

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public Author createAuthor(Author author) {
        log.info("Creating author '{}'", author.getName());

        authorRepository.findByName(author.getName())
                .ifPresent(existingAuthor -> {
                    throw new DuplicateResourceException(
                            "Author with name '" + author.getName() + "' already exists"
                    );
                });

        return authorRepository.save(author);
    }

    public List<Author> getAllAuthors() {
        log.debug("Fetching all authors with books");
        return authorRepository.findAllWithBooks();
    }

    public Page<Author> getAuthorsPage(Pageable pageable) {
        log.debug("Fetching authors page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());
        return authorRepository.findAll(pageable);
    }

    public Author getAuthorById(Long id) {
        log.debug("Fetching author by id {}", id);
        return authorRepository.findByIdWithBooks(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author not found with id: " + id
                ));
    }

    public Author updateAuthor(Long id, Author author) {
        log.info("Updating author id {} to name '{}'", id, author.getName());
        Author existingAuthor = getAuthorById(id);

        authorRepository.findByName(author.getName())
                .ifPresent(foundAuthor -> {
                    if (!foundAuthor.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "Author with name '" + author.getName() + "' already exists"
                        );
                    }
                });

        existingAuthor.setName(author.getName());
        return authorRepository.save(existingAuthor);
    }

    public void deleteAuthor(Long id) {
        Author existingAuthor = getAuthorById(id);

        boolean authorIsUsed = bookRepository.findAllWithAuthorsAndCategory().stream()
                .filter(book -> book.getAuthors() != null)
                .flatMap(book -> book.getAuthors().stream())
                .anyMatch(author -> author.getId().equals(id));

        if (authorIsUsed) {
            log.warn("Cannot delete author '{}' (id={}) because it is associated with books", existingAuthor.getName(), id);
            throw new ResourceInUseException(
                    "Author cannot be deleted because it is associated with one or more books"
            );
        }

        log.info("Deleting author '{}' (id={})", existingAuthor.getName(), id);
        authorRepository.delete(existingAuthor);
    }
}
