package com.unibuc.library.controller;

import com.unibuc.library.model.Book;
import com.unibuc.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
    }

    @Test
    void createBook_Success() {
        // Arrange
        when(bookService.createBook(any(Book.class))).thenReturn(book);

        // Act
        ResponseEntity<Book> response = bookController.createBook(book);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Book", response.getBody().getTitle());
        verify(bookService).createBook(book);
    }

    @Test
    void getAllBooks_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Another Book");

        List<Book> books = Arrays.asList(book, book2);
        when(bookService.getAllBooks()).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(bookService).getAllBooks();
    }

    @Test
    void getBookById_Success() {
        // Arrange
        when(bookService.getBookById(1L)).thenReturn(book);

        // Act
        ResponseEntity<Book> response = bookController.getBookById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(bookService).getBookById(1L);
    }

    @Test
    void deleteBook_Success() {
        // Arrange
        doNothing().when(bookService).deleteBook(1L);

        // Act
        ResponseEntity<Void> response = bookController.deleteBook(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(bookService).deleteBook(1L);
    }

    @Test
    void searchBooksByTitle_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book);
        when(bookService.searchBooksByTitle("Test")).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = bookController.searchBooksByTitle("Test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(bookService).searchBooksByTitle("Test");
    }

    @Test
    void searchBooks_Success() {
        // Arrange
        List<Book> books = Arrays.asList(book);
        when(bookService.searchBooks("Test", null, null)).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = bookController.searchBooks("Test", null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(bookService).searchBooks("Test", null, null);
    }
}