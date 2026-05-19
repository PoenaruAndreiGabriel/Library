package com.unibuc.library.controller;

import com.unibuc.library.model.Author;
import com.unibuc.library.model.Book;
import com.unibuc.library.model.Category;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category category;
    private Book book;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fantasy");

        Author author = new Author();
        author.setId(1L);
        author.setName("J.R.R. Tolkien");

        book = new Book();
        book.setId(1L);
        book.setTitle("The Lord of the Rings");
        book.setIsbn("1234567890");
        book.setCategory(category);
        
        Set<Author> authors = new HashSet<>();
        authors.add(author);
        book.setAuthors(authors);
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        // Act
        ResponseEntity<Category> response = categoryController.createCategory(category);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Fantasy", response.getBody().getName());
        verify(categoryService).createCategory(category);
    }

    @Test
    void createCategory_DifferentCategory_Success() {
        // Arrange
        Category scienceFiction = new Category();
        scienceFiction.setId(2L);
        scienceFiction.setName("Science Fiction");

        when(categoryService.createCategory(any(Category.class))).thenReturn(scienceFiction);

        // Act
        ResponseEntity<Category> response = categoryController.createCategory(scienceFiction);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
        assertEquals("Science Fiction", response.getBody().getName());
        verify(categoryService).createCategory(scienceFiction);
    }

    @Test
    void getAllCategories_Success() {
        // Arrange
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Mystery");

        List<Category> categories = Arrays.asList(category, category2);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Fantasy", response.getBody().get(0).getName());
        assertEquals("Mystery", response.getBody().get(1).getName());
        verify(categoryService).getAllCategories();
    }

    @Test
    void getAllCategories_EmptyList_ReturnsOkWithEmptyList() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(categoryService).getAllCategories();
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        // Act
        ResponseEntity<Category> response = categoryController.getCategoryById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Fantasy", response.getBody().getName());
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void getCategoryById_DifferentId_Success() {
        // Arrange
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Mystery");

        when(categoryService.getCategoryById(2L)).thenReturn(category2);

        // Act
        ResponseEntity<Category> response = categoryController.getCategoryById(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
        assertEquals("Mystery", response.getBody().getName());
        verify(categoryService).getCategoryById(2L);
    }

    @Test
    void getBooksByCategoryName_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("The Hobbit");
        book2.setCategory(category);

        List<Book> books = Arrays.asList(book, book2);
        when(categoryService.getBooksByCategoryName("Fantasy")).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = categoryController.getBooksByCategoryName("Fantasy");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("The Lord of the Rings", response.getBody().get(0).getTitle());
        assertEquals("The Hobbit", response.getBody().get(1).getTitle());
        verify(categoryService).getBooksByCategoryName("Fantasy");
    }

    @Test
    void getBooksByCategoryName_PartialMatch_Success() {
        // Arrange
        when(categoryService.getBooksByCategoryName("fan")).thenReturn(Arrays.asList(book));

        // Act
        ResponseEntity<List<Book>> response = categoryController.getBooksByCategoryName("fan");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Fantasy", response.getBody().get(0).getCategory().getName());
        verify(categoryService).getBooksByCategoryName("fan");
    }

    @Test
    void getBooksByCategoryName_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(categoryService.getBooksByCategoryName("NonExistent")).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Book>> response = categoryController.getBooksByCategoryName("NonExistent");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(categoryService).getBooksByCategoryName("NonExistent");
    }

    @Test
    void getBooksByCategoryName_CaseInsensitive_Success() {
        // Arrange
        when(categoryService.getBooksByCategoryName("FANTASY")).thenReturn(Arrays.asList(book));

        // Act
        ResponseEntity<List<Book>> response = categoryController.getBooksByCategoryName("FANTASY");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(categoryService).getBooksByCategoryName("FANTASY");
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Epic Fantasy");
        when(categoryService.updateCategory(1L, updatedCategory)).thenReturn(updatedCategory);

        // Act
        ResponseEntity<Category> response = categoryController.updateCategory(1L, updatedCategory);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Epic Fantasy", response.getBody().getName());
        verify(categoryService).updateCategory(1L, updatedCategory);
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        doNothing().when(categoryService).deleteCategory(1L);

        // Act
        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void deleteCategory_InUse_ThrowsException() {
        // Arrange
        doThrow(new ResourceInUseException("Category cannot be deleted because it is associated with one or more books"))
                .when(categoryService).deleteCategory(1L);

        // Act & Assert
        ResourceInUseException exception = assertThrows(ResourceInUseException.class,
                () -> categoryController.deleteCategory(1L));

        assertEquals("Category cannot be deleted because it is associated with one or more books", exception.getMessage());
        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void createCategory_ServiceInvoked_Once() {
        // Arrange
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        // Act
        categoryController.createCategory(category);

        // Assert
        verify(categoryService, times(1)).createCategory(category);
    }

    @Test
    void getAllCategories_ServiceInvoked_Once() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category));

        // Act
        categoryController.getAllCategories();

        // Assert
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void getCategoryById_ServiceInvoked_Once() {
        // Arrange
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        // Act
        categoryController.getCategoryById(1L);

        // Assert
        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    void getBooksByCategoryName_ServiceInvoked_Once() {
        // Arrange
        when(categoryService.getBooksByCategoryName("Fantasy")).thenReturn(Arrays.asList(book));

        // Act
        categoryController.getBooksByCategoryName("Fantasy");

        // Assert
        verify(categoryService, times(1)).getBooksByCategoryName("Fantasy");
    }

    @Test
    void getAllCategories_MultipleCategories_Success() {
        // Arrange
        Category fantasy = new Category("Fantasy");
        fantasy.setId(1L);

        Category mystery = new Category("Mystery");
        mystery.setId(2L);

        Category sciFi = new Category("Science Fiction");
        sciFi.setId(3L);

        Category romance = new Category("Romance");
        romance.setId(4L);

        List<Category> categories = Arrays.asList(fantasy, mystery, sciFi, romance);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, response.getBody().size());
        verify(categoryService).getAllCategories();
    }

    @Test
    void getBooksByCategoryName_MultipleBooksInCategory_Success() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("The Hobbit");
        book2.setCategory(category);

        Book book3 = new Book();
        book3.setId(3L);
        book3.setTitle("Harry Potter");
        book3.setCategory(category);

        List<Book> books = Arrays.asList(book, book2, book3);
        when(categoryService.getBooksByCategoryName("Fantasy")).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = categoryController.getBooksByCategoryName("Fantasy");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().stream()
                .allMatch(b -> b.getCategory().getName().equals("Fantasy")));
        verify(categoryService).getBooksByCategoryName("Fantasy");
    }

    @Test
    void createCategory_ReturnsCreatedStatus() {
        // Arrange
        Category newCategory = new Category("Horror");
        newCategory.setId(5L);

        when(categoryService.createCategory(any(Category.class))).thenReturn(newCategory);

        // Act
        ResponseEntity<Category> response = categoryController.createCategory(newCategory);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Horror", response.getBody().getName());
        verify(categoryService).createCategory(newCategory);
    }
}