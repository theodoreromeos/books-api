package com.theodore.morotech.booksapi.clients;

import com.theodore.morotech.booksapi.models.responses.GutendexResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    public GutendexResponse fetchPage(String search, int page) {
        logger.info("Fetching page {} of {}", page, search);

        GutendexResponse body = restClient.get()
                .uri(uri -> uri.path("/books/")
                        .queryParam("search", search)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(GutendexResponse.class);

        logger.info("Raw response: {}", body);

        if (body == null || body.results() == null) {
            throw new IllegalStateException("Gutendex returned an empty body");
        }
        return body;
    }

    public GutendexResponse fallbackFetchPage(String search, int page, Exception ex) {
        throw new IllegalStateException("Gutendex failed for '" + search + "' page " + page + " after retries", ex);
    }

}
