package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.Book;
import com.unibuc.library.model.Category;
import com.unibuc.library.repository.BookRepository;
import com.unibuc.library.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Book book;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fantasy");

        book = new Book();
        book.setId(1L);
        book.setTitle("A Game of Thrones");
        book.setCategory(category);
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = categoryService.createCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fantasy", result.getName());
        verify(categoryRepository).findByName("Fantasy");
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // Arrange
        when(categoryRepository.findByName(category.getName())).thenReturn(Optional.of(category));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> categoryService.createCategory(category));

        assertEquals("Category with name 'Fantasy' already exists", exception.getMessage());
        verify(categoryRepository).findByName("Fantasy");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_Success() {
        // Arrange
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science Fiction");

        List<Category> categories = Arrays.asList(category, category2);
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Fantasy", result.get(0).getName());
        assertEquals("Science Fiction", result.get(1).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        Category result = categoryService.getCategoryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fantasy", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(999L));

        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        Category updated = new Category();
        updated.setName("Epic Fantasy");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName(updated.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = categoryService.updateCategory(1L, updated);

        // Assert
        assertNotNull(result);
        assertEquals("Epic Fantasy", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_DuplicateName_ThrowsException() {
        // Arrange
        Category duplicate = new Category();
        duplicate.setName("Mystery");

        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Mystery");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName(duplicate.getName())).thenReturn(Optional.of(otherCategory));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> categoryService.updateCategory(1L, duplicate));

        assertEquals("Category with name 'Mystery' already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        categoryService.deleteCategory(1L);

        // Assert
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_InUse_ThrowsException() {
        // Arrange
        Book bookInCategory = new Book();
        bookInCategory.setId(2L);
        bookInCategory.setCategory(category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.findAll()).thenReturn(Arrays.asList(bookInCategory));

        // Act & Assert
        ResourceInUseException exception = assertThrows(ResourceInUseException.class,
                () -> categoryService.deleteCategory(1L));

        assertEquals("Category cannot be deleted because it is associated with one or more books", exception.getMessage());
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void getBooksByCategoryName_Success() {
        // Arrange
        Category scifiCategory = new Category();
        scifiCategory.setId(2L);
        scifiCategory.setName("Science Fiction");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Dune");
        book2.setCategory(scifiCategory);

        List<Book> allBooks = Arrays.asList(book, book2);
        when(bookRepository.findAll()).thenReturn(allBooks);

        // Act
        List<Book> result = categoryService.getBooksByCategoryName("Fantasy");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A Game of Thrones", result.get(0).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void getBooksByCategoryName_PartialMatch_Success() {
        // Arrange
        Category fantasyFictionCategory = new Category();
        fantasyFictionCategory.setId(3L);
        fantasyFictionCategory.setName("Fantasy Fiction");

        Book book2 = new Book();
        book2.setId(3L);
        book2.setTitle("The Hobbit");
        book2.setCategory(fantasyFictionCategory);

        List<Book> allBooks = Arrays.asList(book, book2);
        when(bookRepository.findAll()).thenReturn(allBooks);

        // Act - Partial match should return both Fantasy and Fantasy Fiction
        List<Book> result = categoryService.getBooksByCategoryName("Fant");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size()); // Both contain "Fant"
        verify(bookRepository).findAll();
    }

    @Test
    void getBooksByCategoryName_CaseInsensitive_Success() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book));

        // Act - Different cases should still match
        List<Book> result1 = categoryService.getBooksByCategoryName("FANTASY");
        List<Book> result2 = categoryService.getBooksByCategoryName("fantasy");
        List<Book> result3 = categoryService.getBooksByCategoryName("FanTaSy");

        // Assert
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
        verify(bookRepository, times(3)).findAll();
    }

    @Test
    void getBooksByCategoryName_EmptySearchTerm_ThrowsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryService.getBooksByCategoryName(""));

        assertEquals("Category name search term cannot be empty", exception.getMessage());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void getBooksByCategoryName_NullSearchTerm_ThrowsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryService.getBooksByCategoryName(null));

        assertEquals("Category name search term cannot be empty", exception.getMessage());
        verify(bookRepository, never()).findAll();
    }

    @Test
    void getBooksByCategoryName_NoBooksInCategory_ReturnsEmpty() {
        // Arrange
        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Science Fiction");

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Dune");
        book2.setCategory(otherCategory);

        List<Book> allBooks = Arrays.asList(book2);
        when(bookRepository.findAll()).thenReturn(allBooks);

        // Act
        List<Book> result = categoryService.getBooksByCategoryName("Fantasy");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findAll();
    }

    @Test
    void getBooksByCategoryName_BookWithNullCategory_IgnoresBook() {
        // Arrange
        Book bookWithoutCategory = new Book();
        bookWithoutCategory.setId(3L);
        bookWithoutCategory.setTitle("No Category Book");
        bookWithoutCategory.setCategory(null);

        List<Book> allBooks = Arrays.asList(book, bookWithoutCategory);
        when(bookRepository.findAll()).thenReturn(allBooks);

        // Act
        List<Book> result = categoryService.getBooksByCategoryName("Fantasy");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only book with category
        assertEquals("A Game of Thrones", result.get(0).getTitle());
        verify(bookRepository).findAll();
    }
}