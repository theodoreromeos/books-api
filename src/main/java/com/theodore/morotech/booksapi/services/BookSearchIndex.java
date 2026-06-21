package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.clients.GutendexRestClient;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.GutendexSearchResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class BookSearchIndex {

    private final GutendexRestClient client;

    public BookSearchIndex(GutendexRestClient client) {
        this.client = client;
    }

    @Cacheable(cacheNames = "titleMatches", key = "#search", sync = true)
    public List<BookResponse> findAllMatchingBooks(String search) {
        List<String> terms = List.of(search.toLowerCase(Locale.ROOT).split(" "));

        GutendexSearchResponse firstPage = client.fetchPage(search, 1);
        int totalPages = Math.ceilDiv(firstPage.count(), Math.max(firstPage.results().size(), 1));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<GutendexSearchResponse>> pages = IntStream.rangeClosed(2, totalPages)// page 1 was fetched already
                    .mapToObj(page -> executor.submit(() -> client.fetchPage(search, page)))
                    .toList();

            return Stream.concat(Stream.of(firstPage), pages.stream().map(BookSearchIndex::getActualResponse))
                    .flatMap(response -> response.results().stream())
                    .filter(book -> matchesAllTerms(book, terms))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    @Cacheable(cacheNames = "bookById", key = "#bookId", sync = true)
    public BookResponse findBookById(Long bookId) {
        return client.fetchBookById(bookId);
    }

    public boolean bookExists(Long bookId) {
        var book = client.fetchBookById(bookId);
        return book != null && book.title() != null && !book.title().isEmpty();
    }

    private static boolean matchesAllTerms(BookResponse book, List<String> terms) {
        String title = book.title().toLowerCase(Locale.ROOT);
        return terms.stream().allMatch(title::contains);
    }

    private static GutendexSearchResponse getActualResponse(Future<GutendexSearchResponse> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

}