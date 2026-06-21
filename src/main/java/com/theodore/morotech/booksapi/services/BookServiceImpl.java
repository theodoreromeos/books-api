package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.exceptions.BookNotFoundException;
import com.theodore.morotech.booksapi.mappers.BookReviewMapper;
import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import com.theodore.morotech.booksapi.models.responses.FullBookInfoResponse;
import com.theodore.morotech.booksapi.repositories.BookReviewsRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class BookServiceImpl implements BookService {

    private final BookSearchIndex index;
    private final BookReviewMapper mapper;
    private final BookReviewsRepository repository;

    public BookServiceImpl(BookSearchIndex index,
                           BookReviewMapper mapper,
                           BookReviewsRepository repository) {
        this.index = index;
        this.mapper = mapper;
        this.repository = repository;
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

    @Override
    public BookReviewResponse createBookReview(BookReviewRequest request) {
        if (!index.bookExists(request.bookId())) {
            throw new BookNotFoundException(request.bookId());
        }
        var bookReview = mapper.requestToEntity(request);
        var savedBookReview = repository.save(bookReview);
        return mapper.entityToResponse(savedBookReview);
    }

    public FullBookInfoResponse getAllBookInfo(Long bookId) {
        return null;
    }

}
