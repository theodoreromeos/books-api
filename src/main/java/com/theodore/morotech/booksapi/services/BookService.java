package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;

public interface BookService {

    BookSearchResponse searchByTitle(String query, int page, int size);

    BookReviewResponse createBookReview(BookReviewRequest request);

}
