package com.example.demo.mapper;

import com.example.demo.domain.Loan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LoanMapper {
    List<Loan> findAll();
    List<Loan> search(@Param("status") String status);
    int countByBookId(Long bookId);
    Loan findActiveByBookId(Long bookId);
    Loan findActiveByIdForUpdate(Long id);
    int countActiveByBookId(Long bookId);
    int countActiveByStudentId(Long studentId);
    int insert(@Param("bookId") Long bookId, @Param("studentId") Long studentId);
    int markReturned(Long id);
}
