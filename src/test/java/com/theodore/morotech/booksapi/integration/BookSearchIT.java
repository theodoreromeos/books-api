package com.theodore.morotech.booksapi.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class BookSearchIT extends BaseIntegrationTest {

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

        gutendex.stubFor(get(urlPathEqualTo("/"))
                .withQueryParam("search", equalTo("moby"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(okJson(PAGE_1_JSON)));

        gutendex.stubFor(get(urlPathEqualTo("/"))
                .withQueryParam("search", equalTo("moby"))
                .withQueryParam("page", equalTo("2"))
                .willReturn(okJson(PAGE_2_JSON)));
    }

    @Test
    void returnsFilteredAndPaginatedResults() {
        var body = client.get()
                .uri("/api/books/search?title=moby&page=0&size=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookSearchResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.page()).isZero();
        assertThat(body.size()).isEqualTo(5);
        assertThat(body.totalElements()).isEqualTo(6);
        assertThat(body.totalPages()).isEqualTo(2);
        assertThat(body.books()).hasSize(5);

        assertThat(body.books())
                .extracting(BookResponse::id)
                .containsExactly(2701L, 15L, 2702L, 2703L, 2704L);

        assertThat(body.books())
                .extracting(BookResponse::title)
                .noneMatch(t -> t.equalsIgnoreCase("The Whale"));

        var first = body.books().getFirst();
        assertThat(first.title()).contains("Moby Dick");
        assertThat(first.languages()).containsExactly("en");
        assertThat(first.downloadCount()).isEqualTo(120_000);
        assertThat(first.authors())
                .singleElement()
                .satisfies(a -> {
                    assertThat(a.name()).isEqualTo("Melville, Herman");
                    assertThat(a.birthYear()).isEqualTo(1819);
                    assertThat(a.deathYear()).isEqualTo(1891);
                });

        gutendex.verify(2, getRequestedFor(urlPathEqualTo("/")));
    }

    @Test
    void secondSearchForSameTitleIsServedFromRedisCache() {
        client.get()
                .uri("/api/books/search?title=moby&page=0&size=5")
                .exchange()
                .expectStatus().isOk();

        var secondPage = client.get()
                .uri("/api/books/search?title=moby&page=1&size=5")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookSearchResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(secondPage).isNotNull();
        assertThat(secondPage.page()).isEqualTo(1);
        assertThat(secondPage.totalElements()).isEqualTo(6);
        assertThat(secondPage.books())
                .extracting(BookResponse::id)
                .containsExactly(2705L);

        gutendex.verify(2, getRequestedFor(urlPathEqualTo("/")));
    }

    private static final String PAGE_1_JSON = """
        {
          "count": 7,
          "next": null,
          "previous": null,
          "results": [
            {"id": 2701, "title": "Moby Dick; Or, The Whale",
             "authors": [{"name": "Melville, Herman", "birth_year": 1819, "death_year": 1891}],
             "languages": ["en"], "download_count": 120000, "copyright": false, "media_type": "Text"},
            {"id": 15, "title": "Moby-Dick",
             "authors": [{"name": "Melville, Herman", "birth_year": 1819, "death_year": 1891}],
             "languages": ["en"], "download_count": 5000},
            {"id": 2702, "title": "Moby Dick (Annotated)",
             "authors": [{"name": "Melville, Herman", "birth_year": 1819, "death_year": 1891}],
             "languages": ["en"], "download_count": 3000},
            {"id": 900, "title": "The Whale",
             "authors": [{"name": "Anonymous", "birth_year": null, "death_year": null}],
             "languages": ["en"], "download_count": 100}
          ]
        }
        """;

    private static final String PAGE_2_JSON = """
        {
          "count": 7,
          "next": null,
          "previous": null,
          "results": [
            {"id": 2703, "title": "Moby Dick for Children",
             "authors": [{"name": "Melville, Herman", "birth_year": 1819, "death_year": 1891}],
             "languages": ["en"], "download_count": 200},
            {"id": 2704, "title": "Reading Moby Dick",
             "authors": [{"name": "Smith, John", "birth_year": 1950, "death_year": null}],
             "languages": ["en"], "download_count": 150},
            {"id": 2705, "title": "Moby Dick: A Study Guide",
             "authors": [{"name": "Doe, Jane", "birth_year": 1960, "death_year": null}],
             "languages": ["en"], "download_count": 80}
          ]
        }
        """;

}