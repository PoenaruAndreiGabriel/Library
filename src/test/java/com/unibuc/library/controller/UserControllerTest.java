package com.unibuc.library.controller;

import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.service.UserService;
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
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(5);
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Act
        ResponseEntity<User> response = userController.createUser(user);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
        assertEquals(UserRole.MEMBER, response.getBody().getRole());
        assertEquals(5, response.getBody().getMaxBorrowLimit());
        verify(userService).createUser(user);
    }

    @Test
    void createUser_Librarian_ReturnsCreatedStatus() {
        // Arrange
        User librarian = new User();
        librarian.setId(2L);
        librarian.setName("Jane Admin");
        librarian.setEmail("jane.admin@example.com");
        librarian.setRole(UserRole.LIBRARIAN);
        librarian.setMaxBorrowLimit(10);

        when(userService.createUser(any(User.class))).thenReturn(librarian);

        // Act
        ResponseEntity<User> response = userController.createUser(librarian);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
        assertEquals(UserRole.LIBRARIAN, response.getBody().getRole());
        assertEquals(10, response.getBody().getMaxBorrowLimit());
        verify(userService).createUser(librarian);
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole(UserRole.LIBRARIAN);
        user2.setMaxBorrowLimit(10);

        List<User> users = Arrays.asList(user, user2);
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<List<User>> response = userController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
        assertEquals(UserRole.MEMBER, response.getBody().get(0).getRole());
        assertEquals("Jane Smith", response.getBody().get(1).getName());
        assertEquals(UserRole.LIBRARIAN, response.getBody().get(1).getRole());
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_EmptyList_ReturnsOkWithEmptyList() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<User>> response = userController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(user);

        // Act
        ResponseEntity<User> response = userController.getUserById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
        assertEquals(UserRole.MEMBER, response.getBody().getRole());
        assertEquals(5, response.getBody().getMaxBorrowLimit());
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_DifferentId_Success() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole(UserRole.LIBRARIAN);
        user2.setMaxBorrowLimit(10);

        when(userService.getUserById(2L)).thenReturn(user2);

        // Act
        ResponseEntity<User> response = userController.getUserById(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getId());
        assertEquals("Jane Smith", response.getBody().getName());
        assertEquals(UserRole.LIBRARIAN, response.getBody().getRole());
        verify(userService).getUserById(2L);
    }

    @Test
    void createUser_ServiceInvoked_Once() {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(user);

        // Act
        userController.createUser(user);

        // Assert
        verify(userService, times(1)).createUser(user);
    }

    @Test
    void getAllUsers_ServiceInvoked_Once() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user));

        // Act
        userController.getAllUsers();

        // Assert
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ServiceInvoked_Once() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(user);

        // Act
        userController.getUserById(1L);

        // Assert
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getAllUsers_MixedRoles_Success() {
        // Arrange
        User member1 = new User(1L, "Alice Reader", "alice@example.com", UserRole.MEMBER, 5);
        User librarian1 = new User(2L, "Bob Admin", "bob@example.com", UserRole.LIBRARIAN, 15);
        User member2 = new User(3L, "Carol Student", "carol@example.com", UserRole.MEMBER, 3);

        List<User> users = Arrays.asList(member1, librarian1, member2);
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<List<User>> response = userController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals(2, response.getBody().stream()
                .filter(u -> u.getRole() == UserRole.MEMBER)
                .count());
        assertEquals(1, response.getBody().stream()
                .filter(u -> u.getRole() == UserRole.LIBRARIAN)
                .count());
        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_Success() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("John Updated");
        updatedUser.setEmail("john.updated@example.com");
        updatedUser.setRole(UserRole.LIBRARIAN);
        updatedUser.setMaxBorrowLimit(7);

        when(userService.updateUser(1L, updatedUser)).thenReturn(updatedUser);

        // Act
        ResponseEntity<User> response = userController.updateUser(1L, updatedUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Updated", response.getBody().getName());
        verify(userService).updateUser(1L, updatedUser);
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_InUse_ThrowsException() {
        // Arrange
        doThrow(new ResourceInUseException("User cannot be deleted because it has loan history"))
                .when(userService).deleteUser(1L);

        // Act & Assert
        ResourceInUseException exception = assertThrows(ResourceInUseException.class,
                () -> userController.deleteUser(1L));

        assertEquals("User cannot be deleted because it has loan history", exception.getMessage());
        verify(userService).deleteUser(1L);
    }
}