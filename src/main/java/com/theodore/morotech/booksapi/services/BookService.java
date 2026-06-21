package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import com.theodore.morotech.booksapi.models.responses.BookFullInfoResponse;

import java.util.List;

public interface BookService {

    BookSearchResponse searchByTitle(String query, int page, int size);

    BookReviewResponse createBookReview(BookReviewRequest request);

    BookFullInfoResponse fetchBookFullInfo(Long bookId);

    List<BookFullInfoResponse> fetchTopBooks(Integer count);

}
