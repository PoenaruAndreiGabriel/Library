package com.unibuc.library.config;

import com.unibuc.library.model.User;
import com.unibuc.library.model.UserRole;
import com.unibuc.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadInitialUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User librarian = new User();
                librarian.setName("Head Librarian");
                librarian.setEmail("librarian@local");
                librarian.setRole(UserRole.LIBRARIAN);
                librarian.setMaxBorrowLimit(20);
                librarian.setPassword(passwordEncoder.encode("lib12345"));
                librarian.setEnabled(true);

                User member = new User();
                member.setName("Demo Member");
                member.setEmail("member@local");
                member.setRole(UserRole.MEMBER);
                member.setMaxBorrowLimit(5);
                member.setPassword(passwordEncoder.encode("member123"));
                member.setEnabled(true);
                User realAdmin = new User();
                realAdmin.setName("Site Admin");
                realAdmin.setEmail("admin@admin");
                realAdmin.setRole(UserRole.ADMIN);
                realAdmin.setMaxBorrowLimit(99);
                realAdmin.setPassword(passwordEncoder.encode("adminadmin"));
                realAdmin.setEnabled(true);
                userRepository.save(librarian);
                userRepository.save(member);
                userRepository.save(realAdmin);

                log.info("=== INITIAL CREDENTIALS ===");
                log.info("ADMIN: admin@admin / adminadmin");
                log.info("LIBRARIAN: librarian@local / lib12345");
                log.info("MEMBER: member@local / member123");
                log.info("==========================");
            }
        };
    }
}


