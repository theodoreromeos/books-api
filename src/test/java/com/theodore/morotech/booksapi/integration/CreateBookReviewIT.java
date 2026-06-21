package com.theodore.morotech.booksapi.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import com.theodore.morotech.booksapi.repositories.BookReviewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class CreateBookReviewIT extends BaseIntegrationTest {

    @Autowired
    BookReviewsRepository bookReviewsRepository;

    @RegisterExtension
    static WireMockExtension gutendex = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void gutendexUrl(DynamicPropertyRegistry registry) {
        registry.add("gutendex.url", gutendex::baseUrl);
    }

    @BeforeEach
    void stubGutendex() {
        gutendex.resetAll();

        gutendex.stubFor(get(urlPathEqualTo("/2701"))
                .willReturn(okJson(BOOK_2701_JSON)));

        gutendex.stubFor(get(urlPathEqualTo("/9999"))
                .willReturn(aResponse().withStatus(404)));
    }

    @Test
    void savesReviewAndReturnsBookReviewResponse() {
        var request = new BookReviewRequest(2701L, 5, "A timeless classic");

        var body = client.post()
                .uri("/api/books/review")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookReviewResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.bookId()).isEqualTo(2701L);
        assertThat(body.rating()).isEqualTo(5);
        assertThat(body.review()).isEqualTo("A timeless classic");

        gutendex.verify(1, getRequestedFor(urlPathEqualTo("/2701")));
    }

    @Test
    @DisplayName("createBookReview: given an existing book when creating a book review the review is persisted in the database (positive scenario)")
    void givenExistingBook_whenCreatingBookReview_thenReviewIsPersistedInDatabase() {
        // given
        long countBefore = bookReviewsRepository.count();

        // when
        client.post()
                .uri("/api/books/review")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BookReviewRequest(2701L, 4, "best review evah"))
                .exchange()
                .expectStatus().isOk();

        // then
        long countAfter = bookReviewsRepository.count();
        assertThat(countAfter).isGreaterThan(countBefore);

        assertThat(bookReviewsRepository.findAll())
                .anyMatch(r -> r.getBookId().equals(2701L)
                        && r.getRating() == 4
                        && r.getReview().equals("best review evah"));
    }

    @Test
    void returnsNotFoundWhenBookDoesNotExist() {
        var request = new BookReviewRequest(9999L, 3, "Some review");

        client.post()
                .uri("/api/books/review")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isNotFound();

        gutendex.verify(1, getRequestedFor(urlPathEqualTo("/9999")));
    }

    private static final String BOOK_2701_JSON = """
            {
              "id": 2701,
              "title": "Moby Dick; Or, The Whale",
              "authors": [{"name": "Melville, Herman", "birth_year": 1819, "death_year": 1891}],
              "languages": ["en"],
              "download_count": 120000
            }
            """;

}