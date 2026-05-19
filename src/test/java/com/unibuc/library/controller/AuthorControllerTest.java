package com.unibuc.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Author;
import com.unibuc.library.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Author author;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authorController).build();
        objectMapper = new ObjectMapper();

        author = new Author();
        author.setId(1L);
        author.setName("George R.R. Martin");
    }

    @Test
    void createAuthor_Success() throws Exception {
        // Arrange
        when(authorService.createAuthor(any(Author.class))).thenReturn(author);

        // Act & Assert using MockMvc
        mockMvc.perform(post("/rest/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("George R.R. Martin"));

        verify(authorService).createAuthor(any(Author.class));
    }

    @Test
    void createAuthor_DuplicateName_ReturnsConflict() throws Exception {
        // Arrange
        when(authorService.createAuthor(any(Author.class)))
                .thenThrow(new DuplicateResourceException("Author with name 'George R.R. Martin' already exists"));

        // Act & Assert
        mockMvc.perform(post("/rest/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DuplicateResourceException))
                .andExpect(result -> assertEquals("Author with name 'George R.R. Martin' already exists",
                        result.getResolvedException().getMessage()));

        verify(authorService).createAuthor(any(Author.class));
    }

    @Test
    void createAuthor_InvalidData_ReturnsBadRequest() throws Exception {
        // Arrange - Empty name
        Author invalidAuthor = new Author();
        invalidAuthor.setName("");

        // Act & Assert - @Valid annotation will cause validation error
        mockMvc.perform(post("/rest/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).createAuthor(any(Author.class));
    }

    @Test
    void createAuthor_NullRequestBody_ThrowsException() throws Exception {
        // Arrange - Empty body
        // Act & Assert
        mockMvc.perform(post("/rest/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).createAuthor(any(Author.class));
    }

    @Test
    void getAllAuthors_Success() throws Exception {
        // Arrange
        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("J.K. Rowling");

        List<Author> authors = Arrays.asList(author, author2);
        when(authorService.getAllAuthors()).thenReturn(authors);

        // Act & Assert
        mockMvc.perform(get("/rest/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("George R.R. Martin"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("J.K. Rowling"));

        verify(authorService).getAllAuthors();
    }

    @Test
    void getAllAuthors_EmptyList_Success() throws Exception {
        // Arrange
        when(authorService.getAllAuthors()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/rest/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(authorService).getAllAuthors();
    }

    @Test
    void getAuthorById_Success() throws Exception {
        // Arrange
        when(authorService.getAuthorById(1L)).thenReturn(author);

        // Act & Assert
        mockMvc.perform(get("/rest/authors/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("George R.R. Martin"));

        verify(authorService).getAuthorById(1L);
    }

    @Test
    void getAuthorById_NotFound_ThrowsException() throws Exception {
        // Arrange
        when(authorService.getAuthorById(999L))
                .thenThrow(new ResourceNotFoundException("Author not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/rest/authors/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
                .andExpect(result -> assertEquals("Author not found with id: 999",
                        result.getResolvedException().getMessage()));

        verify(authorService).getAuthorById(999L);
    }

    @Test
    void getAuthorById_InvalidId_ThrowsException() throws Exception {
        // Arrange
        when(authorService.getAuthorById(-1L))
                .thenThrow(new ResourceNotFoundException("Author not found with id: -1"));

        // Act & Assert
        mockMvc.perform(get("/rest/authors/{id}", -1L))
                .andExpect(status().isNotFound());

        verify(authorService).getAuthorById(-1L);
    }

    @Test
    void getAuthorById_ZeroId_ThrowsException() throws Exception {
        // Arrange
        when(authorService.getAuthorById(0L))
                .thenThrow(new ResourceNotFoundException("Author not found with id: 0"));

        // Act & Assert
        mockMvc.perform(get("/rest/authors/{id}", 0L))
                .andExpect(status().isNotFound());

        verify(authorService).getAuthorById(0L);
    }

    @Test
    void getAuthorById_WithNullPath_ReturnsBadRequest() throws Exception {
        // This test checks the behavior when path variable is missing (not null)
        // Spring will return 400 Bad Request if path variable is missing
        mockMvc.perform(get("/rest/authors/"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createAuthor_WithFullConstructor_Success() throws Exception {
        // Arrange
        Author newAuthor = new Author("Brandon Sanderson");
        newAuthor.setId(3L);

        when(authorService.createAuthor(any(Author.class))).thenReturn(newAuthor);

        // Act & Assert
        mockMvc.perform(post("/rest/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuthor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Brandon Sanderson"));

        verify(authorService).createAuthor(any(Author.class));
    }

    // Additional test for validation
    @Test
    void createAuthor_WithNullName_ReturnsBadRequest() throws Exception {
        // Arrange - Author with null name
        Author invalidAuthor = new Author();
        invalidAuthor.setId(4L);
        // name is null

        // Act & Assert - @Valid should fail on @NotBlank constraint
        mockMvc.perform(post("/rest/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest());

        verify(authorService, never()).createAuthor(any(Author.class));
    }
}