package com.example.demo.mapper;

import com.example.demo.domain.Book;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BookMapper {
    List<Book> findAll();
    List<Book> search(@Param("pattern") String pattern, @Param("available") Boolean available);
    List<Book> findAvailable();
    Book findById(Long id);
    Book findByIdForUpdate(Long id);
    int countByIsbn(String isbn);
    int insert(Book book);
    int delete(Long id);
    int updateAvailability(@Param("id") Long id, @Param("available") boolean available);
}
