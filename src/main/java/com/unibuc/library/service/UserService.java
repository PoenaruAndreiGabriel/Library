package com.unibuc.library.service;

import com.unibuc.library.exception.DuplicateResourceException;
import com.unibuc.library.exception.ResourceInUseException;
import com.unibuc.library.exception.ResourceNotFoundException;
import com.unibuc.library.model.User;
import com.unibuc.library.model.UserProfile;
import com.unibuc.library.repository.LoanRepository;
import com.unibuc.library.repository.ReservationRepository;
import com.unibuc.library.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       LoanRepository loanRepository,
                       ReservationRepository reservationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        log.info("Creating user '{}' with email '{}'", user.getName(), user.getEmail());

        userRepository.findByEmail(user.getEmail())
                .ifPresent(existingUser -> {
                    throw new DuplicateResourceException(
                            "User with email '" + user.getEmail() + "' already exists"
                    );
                });

        // encode password before saving
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getProfile() != null) {
            user.getProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        log.debug("Fetching user by id {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));
    }

    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    public Page<User> getUsersPage(Pageable pageable) {
        log.debug("Fetching users page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    public User updateUser(Long id, User user) {
        log.info("Updating user id {} to name '{}' and email '{}'", id, user.getName(), user.getEmail());
        User existingUser = getUserById(id);

        userRepository.findByEmail(user.getEmail())
                .ifPresent(foundUser -> {
                    if (foundUser.getId() != id) {
                        throw new DuplicateResourceException(
                                "User with email '" + user.getEmail() + "' already exists"
                        );
                    }
                });

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        existingUser.setMaxBorrowLimit(user.getMaxBorrowLimit());
        existingUser.setProfile(user.getProfile());

        // Update password only if provided (and encode it)
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        UserProfile profile = existingUser.getProfile();
        if (profile != null) {
            profile.setUser(existingUser);
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = getUserById(id);

        if (loanRepository.existsByUserId(id)) {
            log.warn("Cannot delete user '{}' (id={}) because it has loan history", existingUser.getName(), id);
            throw new ResourceInUseException("User cannot be deleted because it has loan history");
        }
        if (reservationRepository.existsByUserId(id)) {
            log.warn("Cannot delete user '{}' (id={}) because it has reservations", existingUser.getName(), id);
            throw new ResourceInUseException("User cannot be deleted because it has reservations");
        }

        log.info("Deleting user '{}' (id={})", existingUser.getName(), id);
        userRepository.delete(existingUser);
    }
}
