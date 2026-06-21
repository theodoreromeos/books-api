package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;

public interface BookService {

    BookSearchResponse searchByTitle(String query, int page, int size);

}
