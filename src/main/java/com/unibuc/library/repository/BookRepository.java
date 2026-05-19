package com.unibuc.library.repository;

import com.unibuc.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    /**
     * Fetches all books together with their authors and category in a single query,
     * avoiding LazyInitializationException when the session is closed in the view layer.
     */
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.category")
    List<Book> findAllWithAuthorsAndCategory();

    @Override
    @EntityGraph(attributePaths = {"authors", "category"})
    Page<Book> findAll(Pageable pageable);

    /**
     * Fetches a single book with authors and category eagerly.
     */
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.category WHERE b.id = :id")
    Optional<Book> findByIdWithAuthorsAndCategory(@Param("id") Long id);
}
