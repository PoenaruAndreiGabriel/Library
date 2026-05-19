package com.unibuc.library.repository;

import com.unibuc.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByReturnDateIsNull();

    long countByUserIdAndReturnDateIsNull(Long userId);

    boolean existsByBookId(Long bookId);

    boolean existsByUserId(Long userId);
}
