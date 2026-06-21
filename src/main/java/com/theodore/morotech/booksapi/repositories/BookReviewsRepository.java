package com.theodore.morotech.booksapi.repositories;

import com.theodore.morotech.booksapi.entities.BookReviews;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookReviewsRepository extends JpaRepository<BookReviews, Long> {

    List<BookReviews> findByBookId(Long bookId);

    @Query("SELECT br.bookId FROM BookReviews br GROUP BY br.bookId ORDER BY AVG(br.rating) DESC")
    List<Long> findTopBookIdsByAverageRating(Pageable pageable);

}
