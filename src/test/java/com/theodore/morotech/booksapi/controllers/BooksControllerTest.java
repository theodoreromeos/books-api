package com.theodore.morotech.booksapi.controllers;

import com.theodore.morotech.booksapi.models.responses.AuthorResponse;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import com.theodore.morotech.booksapi.services.BookService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

}