package com.theodore.morotech.booksapi.clients;

import com.theodore.morotech.booksapi.exceptions.BookNotFoundException;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.GutendexSearchResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GutendexRestClient {

    private static final Logger logger = LoggerFactory.getLogger(GutendexRestClient.class);

    private final RestClient restClient;

    public GutendexRestClient(RestClient.Builder builder,
                              @Value("${gutendex.url}") String gutendexUrl) {
        this.restClient = builder
                .baseUrl(gutendexUrl)
                .build();
    }

    @Retry(name = "gutendex", fallbackMethod = "fallbackFetchPage")
    public GutendexSearchResponse fetchPage(String search, int page) {
        logger.info("Fetching page {} of {}", page, search);

        GutendexSearchResponse body = restClient.get()
                .uri(uri -> uri.path("/")
                        .queryParam("search", search)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(GutendexSearchResponse.class);

        if (body == null || body.results() == null) {
            throw new IllegalStateException("Gutendex returned an empty body");
        }
        return body;
    }

    public GutendexSearchResponse fallbackFetchPage(String search, int page, Exception ex) {
        throw new IllegalStateException("Gutendex failed for '" + search + "' page " + page + " after retries", ex);
    }

    public BookResponse fetchBookById(Long bookId) {
        logger.info("Fetching book with id : {}", bookId);
        return restClient.get()
                .uri("/{id}", bookId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    if (res.getStatusCode().value() == 404) {
                        throw new BookNotFoundException(bookId);
                    }
                })
                .body(BookResponse.class);
    }

}
