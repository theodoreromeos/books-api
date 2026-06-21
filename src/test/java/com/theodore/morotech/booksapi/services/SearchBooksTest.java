package com.theodore.morotech.booksapi.services;

import com.theodore.morotech.booksapi.clients.GutendexRestClient;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.GutendexSearchResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchBooksTest {

    @Nested
    class BookServiceTests {

        @Mock
        BookSearchIndex index;
        @InjectMocks
        BookServiceImpl service;

        @Test
        void returnsRequestedPageSlice() {
            // given
            when(index.findAllMatchingBooks("dog")).thenReturn(createBooks(1, 2, 3, 4, 5));

            // when
            var response = service.searchByTitle("dog", 1, 2);

            // then
            assertThat(response.books()).extracting(BookResponse::id).containsExactly(3L, 4L);
            assertThat(response.totalElements()).isEqualTo(5);
            assertThat(response.totalPages()).isEqualTo(3);
        }

        @Test
        void pageBeyondEndReturnsEmptyContentButCorrectMetadata() {
            // given
            when(index.findAllMatchingBooks("dog")).thenReturn(createBooks(1, 2, 3));

            // when
            var response = service.searchByTitle("dog", 5, 2);

            // then
            assertThat(response.books()).isEmpty();
            assertThat(response.totalPages()).isEqualTo(2);
        }

        private List<BookResponse> createBooks(long... ids) {
            return Arrays.stream(ids)
                    .mapToObj(id -> new BookResponse(
                            id,
                            "title " + id,
                            List.of(),          // authors
                            List.of("en"),  // languages
                            0))                 // downloadCount
                    .toList();
        }


    }

    @Nested
    class BookSearchIndexTests {

        @Mock
        GutendexRestClient client;

        @InjectMocks
        BookSearchIndex index;

        @Test
        void fetchesEveryPageAndAggregatesTitleMatches() {
            // given
            when(client.fetchPage("dog", 1)).thenReturn(mockGutendexResults(3, createBook(1, "Test Dog 1"), createBook(2, "Test Cat")));
            when(client.fetchPage("dog", 2)).thenReturn(mockGutendexResults(3, createBook(3, "Test Dog 3")));

            // when
            List<BookResponse> result = index.findAllMatchingBooks("dog");

            // then
            assertThat(result).extracting(BookResponse::id).containsExactly(1L, 3L);
            verify(client).fetchPage("dog", 1);
            verify(client).fetchPage("dog", 2);
            verify(client, never()).fetchPage("dog", 3);
        }

        @Test
        void keepsOnlyBooksWhoseTitleContainsAllTerms() {
            // given
            when(client.fetchPage("dog cat", 1)).thenReturn(mockGutendexResults(3,
                    createBook(1, "Dog and Cat"),
                    createBook(2, "Just a Dog"),
                    createBook(3, "Cat Stories")));

            // when
            List<BookResponse> result = index.findAllMatchingBooks("dog cat");

            // then
            assertThat(result).extracting(BookResponse::id).containsExactly(1L);
            verify(client, never()).fetchPage("dog cat", 2);
        }

        @Test
        void findAllMatchingBooks_throwsIllegalStateException_whenFetchingAnotherPageFails() {
            // given
            String search = "pride prejudice";

            BookResponse firstPageBook = createBook(1, "test book");

            GutendexSearchResponse firstPage = mockGutendexResults(2, firstPageBook);

            RuntimeException apiFailure = new RuntimeException("Gutendex down");

            when(client.fetchPage(search, 1)).thenReturn(firstPage);
            when(client.fetchPage(search, 2)).thenThrow(apiFailure);

            // when + then
            assertThatThrownBy(() -> index.findAllMatchingBooks(search))
                    .isInstanceOf(IllegalStateException.class)
                    .hasCause(apiFailure);

            verify(client).fetchPage(search, 1);
            verify(client).fetchPage(search, 2);
            verifyNoMoreInteractions(client);
        }

        private static BookResponse createBook(long id, String title) {
            return new BookResponse(id, title, List.of(), List.of("en"), 0);
        }

        private static GutendexSearchResponse mockGutendexResults(int count, BookResponse... books) {
            return new GutendexSearchResponse(count, null, null, List.of(books));
        }

    }

}
