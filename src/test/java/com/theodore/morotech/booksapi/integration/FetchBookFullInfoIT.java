package com.theodore.morotech.booksapi.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.theodore.morotech.booksapi.entities.BookReviews;
import com.theodore.morotech.booksapi.models.responses.BookFullInfoResponse;
import com.theodore.morotech.booksapi.repositories.BookReviewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class FetchBookFullInfoIT extends BaseIntegrationTest {

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
    void setUp() {
        gutendex.resetAll();
        bookReviewsRepository.deleteAll();

        gutendex.stubFor(get(urlPathEqualTo("/2701"))
                .willReturn(okJson(BOOK_2701_JSON)));
    }

    @Test
    void returnsZeroRatingAndEmptyReviewsWhenNoReviewsExist() {
        var body = client.get()
                .uri("/api/books/review/2701")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookFullInfoResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.reviews()).isEmpty();
        assertThat(body.rating()).isEqualByComparingTo(BigDecimal.ZERO);

        gutendex.verify(1, getRequestedFor(urlPathEqualTo("/2701")));
    }

    @Test
    void returnsAverageRatingAndReviewTextsWhenReviewsExist() {
        // given
        var review1 = new BookReviews();
        review1.setBookId(2701L);
        review1.setRating(4);
        review1.setReview("Brilliant");

        var review2 = new BookReviews();
        review2.setBookId(2701L);
        review2.setRating(2);
        review2.setReview("Average");

        bookReviewsRepository.saveAll(List.of(review1, review2));

        // when
        var body = client.get()
                .uri("/api/books/review/2701")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookFullInfoResponse.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(body).isNotNull();
        assertThat(body.reviews()).containsExactlyInAnyOrder("Brilliant", "Average");
        assertThat(body.rating()).isEqualByComparingTo(new BigDecimal("3.00"));

        gutendex.verify(1, getRequestedFor(urlPathEqualTo("/2701")));
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