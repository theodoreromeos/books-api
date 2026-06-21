package com.theodore.morotech.booksapi.controllers;

import com.theodore.morotech.booksapi.exceptions.BookNotFoundException;
import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.AuthorResponse;
import com.theodore.morotech.booksapi.models.responses.BookFullInfoResponse;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import com.theodore.morotech.booksapi.services.BookService;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BooksController.class)
class BooksControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Nested
    class SearchBooksByTitle {

        @Test
        void search_returnsBookSearchResponse() throws Exception {
            // given
            var authors = List.of(new AuthorResponse(1815, 1852, "Ada Lovelace"));
            var books = List.of(new BookResponse(1L, "Notes", authors, List.of("en"), 5000));
            var response = new BookSearchResponse(books, 0, 20, 1, 1);

            when(bookService.searchByTitle("Notes", 0, 20)).thenReturn(response);

            // when + then
            mockMvc.perform(get("/api/books/search").param("title", "Notes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.total_elements").value(1))
                    .andExpect(jsonPath("$.total_pages").value(1))
                    .andExpect(jsonPath("$.books[0].id").value(1))
                    .andExpect(jsonPath("$.books[0].title").value("Notes"))
                    .andExpect(jsonPath("$.books[0].authors[0].name").value("Ada Lovelace"))
                    .andExpect(jsonPath("$.books[0].languages[0]").value("en"))
                    .andExpect(jsonPath("$.books[0].download_count").value(5000));
        }

        @Test
        void search_appliesDefaultPaginationWhenNotProvided() throws Exception {
            // given
            when(bookService.searchByTitle("Java", 0, 20))
                    .thenReturn(new BookSearchResponse(List.of(), 0, 20, 0, 0));

            // when + then
            mockMvc.perform(get("/api/books/search").param("title", "Java"))
                    .andExpect(status().isOk());

            verify(bookService).searchByTitle("Java", 0, 20);
        }

        @Test
        void search_forwardsCustomPaginationToService() throws Exception {
            // given
            when(bookService.searchByTitle("Spring", 2, 5))
                    .thenReturn(new BookSearchResponse(List.of(), 2, 5, 0, 0));

            // when + then
            mockMvc.perform(get("/api/books/search")
                            .param("title", "Spring")
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk());

            verify(bookService).searchByTitle("Spring", 2, 5);
        }

        @Test
        void search_returnsBadRequestWhenTitleIsMissing() throws Exception {
            // when + then
            mockMvc.perform(get("/api/books/search"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void search_returnsEmptyContentList() throws Exception {
            // given
            when(bookService.searchByTitle("Unknown", 0, 20))
                    .thenReturn(new BookSearchResponse(List.of(), 0, 20, 0, 0));

            // when + then
            mockMvc.perform(get("/api/books/search").param("title", "Unknown"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.books").isEmpty())
                    .andExpect(jsonPath("$.total_elements").value(0))
                    .andExpect(jsonPath("$.total_pages").value(0));
        }

        @Test
        void search_returnsBadRequestWhenTitleIsBlank() throws Exception {
            // when + then
            mockMvc.perform(get("/api/books/search").param("title", "  "))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void search_returnsBadRequestWhenPageIsNegative() throws Exception {
            // when + then
            mockMvc.perform(get("/api/books/search")
                            .param("title", "Java")
                            .param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void search_returnsBadRequestWhenSizeIsBelowMinimum() throws Exception {
            // when + then
            mockMvc.perform(get("/api/books/search")
                            .param("title", "Java")
                            .param("size", "4"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void search_returnsBadRequestWhenSizeExceedsMaximum() throws Exception {
            // when + then
            mockMvc.perform(get("/api/books/search")
                            .param("title", "Java")
                            .param("size", "101"))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class CreateBookReviewTests {

        @Test
        void createBookReview_returnsBookReviewResponse() throws Exception {
            // given
            var request = new BookReviewRequest(1L, 5, "Excellent read");
            var response = new BookReviewResponse(1L, 5, "Excellent read");

            when(bookService.createBookReview(request)).thenReturn(response);

            // when + then
            mockMvc.perform(post("/api/books/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"book_id": 1, "rating": 5, "review": "Excellent read"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.book_id").value(1))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.review").value("Excellent read"));
        }

        @Test
        void createBookReview_returnsBadRequestWhenBookIdIsNull() throws Exception {
            // when + then
            mockMvc.perform(post("/api/books/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"book_id": null, "rating": 3, "review": "test review"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createBookReview_returnsBadRequestWhenRatingIsOutOfRange() throws Exception {
            // when + then
            mockMvc.perform(post("/api/books/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"book_id": 1, "rating": 0, "review": "test review"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class FetchBookFullInfoTests {

        @Test
        void fetchBookFullInfo_returnsBookFullInfoResponse() throws Exception {
            // given
            var authors = List.of(new AuthorResponse(1819, 1891, "Melville, Herman"));
            var book = new BookResponse(1L, "Moby Dick", authors, List.of("en"), 120000);
            var response = new BookFullInfoResponse(book, List.of("A classic"), new BigDecimal("4.50"));

            when(bookService.fetchBookFullInfo(1L)).thenReturn(response);

            // when + then
            mockMvc.perform(get("/api/books/review/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Moby Dick"))
                    .andExpect(jsonPath("$.reviews[0]").value("A classic"))
                    .andExpect(jsonPath("$.rating").value(4.5));
        }

        @Test
        void fetchBookFullInfo_returnsNotFoundWhenBookDoesNotExist() throws Exception {
            // given
            when(bookService.fetchBookFullInfo(9999L)).thenThrow(new BookNotFoundException(9999L));

            // when + then
            mockMvc.perform(get("/api/books/review/9999"))
                    .andExpect(status().isNotFound());
        }

    }

}