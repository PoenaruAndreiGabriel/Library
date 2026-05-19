package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Author;
import com.unibuc.library.model.Book;
import com.unibuc.library.repository.AuthorRepository;
import com.unibuc.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1L);
        author.setName("George R.R. Martin");
    }

    @Test
    void createAuthor_Success() {
        // Arrange
        when(authorRepository.findByName(author.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        // Act
        Author result = authorService.createAuthor(author);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("George R.R. Martin", result.getName());
        verify(authorRepository).findByName("George R.R. Martin");
        verify(authorRepository).save(author);
    }

    @Test
    void createAuthor_DuplicateName_ThrowsException() {
        // Arrange
        when(authorRepository.findByName(author.getName())).thenReturn(Optional.of(author));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> authorService.createAuthor(author));

        assertEquals("Author with name 'George R.R. Martin' already exists", exception.getMessage());
        verify(authorRepository).findByName("George R.R. Martin");
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void getAllAuthors_Success() {
        // Arrange
        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("Suzanne Collins");

        List<Author> authors = Arrays.asList(author, author2);
        when(authorRepository.findAll()).thenReturn(authors);

        // Act
        List<Author> result = authorService.getAllAuthors();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("George R.R. Martin", result.get(0).getName());
        assertEquals("Suzanne Collins", result.get(1).getName());
        verify(authorRepository).findAll();
    }

    @Test
    void getAuthorById_Success() {
        // Arrange
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        // Act
        Author result = authorService.getAuthorById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("George R.R. Martin", result.getName());
        verify(authorRepository).findById(1L);
    }

    @Test
    void getAuthorById_NotFound_ThrowsException() {
        // Arrange
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authorService.getAuthorById(999L));

        assertEquals("Author not found with id: 999", exception.getMessage());
        verify(authorRepository).findById(999L);
    }

    @Test
    void updateAuthor_Success() {
        // Arrange
        Author updated = new Author();
        updated.setName("George R. R. Martin");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByName(updated.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        // Act
        Author result = authorService.updateAuthor(1L, updated);

        // Assert
        assertNotNull(result);
        assertEquals("George R. R. Martin", result.getName());
        verify(authorRepository).findById(1L);
        verify(authorRepository).save(author);
    }

    @Test
    void updateAuthor_DuplicateName_ThrowsException() {
        // Arrange
        Author duplicate = new Author();
        duplicate.setName("J.K. Rowling");

        Author otherAuthor = new Author();
        otherAuthor.setId(2L);
        otherAuthor.setName("J.K. Rowling");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByName(duplicate.getName())).thenReturn(Optional.of(otherAuthor));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> authorService.updateAuthor(1L, duplicate));

        assertEquals("Author with name 'J.K. Rowling' already exists", exception.getMessage());
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void deleteAuthor_Success() {
        // Arrange
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        authorService.deleteAuthor(1L);

        // Assert
        verify(authorRepository).findById(1L);
        verify(authorRepository).delete(author);
    }

    @Test
    void deleteAuthor_InUse_ThrowsException() {
        // Arrange
        Book book = new Book();
        book.setId(1L);
        Set<Author> authors = new HashSet<>();
        authors.add(author);
        book.setAuthors(authors);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book));

        // Act & Assert
        ResourceInUseException exception = assertThrows(ResourceInUseException.class,
                () -> authorService.deleteAuthor(1L));

        assertEquals("Author cannot be deleted because it is associated with one or more books", exception.getMessage());
        verify(authorRepository, never()).delete(any(Author.class));
    }
}