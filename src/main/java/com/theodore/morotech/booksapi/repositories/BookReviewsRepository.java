package com.theodore.morotech.booksapi.repositories;

import com.theodore.morotech.booksapi.entities.BookReviews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookReviewsRepository extends JpaRepository<BookReviews, Long> {

    List<BookReviews> findByBookId(Long bookId);


}
