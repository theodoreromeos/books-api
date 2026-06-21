package com.theodore.morotech.booksapi.repositories;

import com.theodore.morotech.booksapi.entities.BookReviews;
import com.theodore.morotech.booksapi.models.responses.MonthlyRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookReviewsRepository extends JpaRepository<BookReviews, Long> {

    List<BookReviews> findByBookId(Long bookId);

    @Query("SELECT br.bookId FROM BookReviews br GROUP BY br.bookId ORDER BY AVG(br.rating) DESC")
    List<Long> findTopBookIdsByAverageRating(Pageable pageable);

    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM') AS month, ROUND(AVG(rating), 2) AS average_rating " +
                   "FROM book_reviews WHERE book_id = :bookId " +
                   "GROUP BY TO_CHAR(created_at, 'YYYY-MM') ORDER BY month",
           nativeQuery = true)
    List<MonthlyRating> findMonthlyAverageRatingByBookId(@Param("bookId") Long bookId);

}
