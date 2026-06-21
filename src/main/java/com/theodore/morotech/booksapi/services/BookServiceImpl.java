package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class BookServiceImpl implements BookService {

    private final BookSearchIndex index;

    public BookServiceImpl(BookSearchIndex index) {
        this.index = index;
    }

    @Override
    public BookSearchResponse searchByTitle(String query, int page, int size) {

        List<String> terms = Arrays.stream(query.strip().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(s -> !s.isBlank()).toList();

        if (terms.isEmpty()) {
            return new BookSearchResponse(List.of(), page, size, 0, 0);
        }

        List<BookResponse> all = index.findAllMatchingBooks(String.join(" ", terms));   // cached full list

        int totalElements = all.size();
        int totalPages = (totalElements + size - 1) / size;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<BookResponse> books = from >= totalElements ? List.of() : all.subList(from, to);

        return new BookSearchResponse(List.copyOf(books), page, size, totalElements, totalPages);
    }

}
