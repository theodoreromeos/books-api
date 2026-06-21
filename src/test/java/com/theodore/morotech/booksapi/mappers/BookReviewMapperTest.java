package com.theodore.morotech.booksapi.mappers;

import com.theodore.morotech.booksapi.entities.BookReviews;
import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.AuthorResponse;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookReviewMapperTest {

    BookReviewMapper mapper = new BookReviewMapperImpl();

    @Test
    void requestToEntity_mapsAllFieldsAndIgnoresIdAndCreatedAt() {
        // given
        var request = new BookReviewRequest(42L, 5, "review_test");

        // when
        BookReviews entity = mapper.requestToEntity(request);

        // then
        assertThat(entity.getBookId()).isEqualTo(42L);
        assertThat(entity.getRating()).isEqualTo(5);
        assertThat(entity.getReview()).isEqualTo("review_test");
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    void entityToResponse_mapsAllFields() {
        // given
        var entity = new BookReviews();
        entity.setBookId(42L);
        entity.setRating(5);
        entity.setReview("review_test");

        // when
        BookReviewResponse response = mapper.entityToResponse(entity);

        // then
        assertThat(response.bookId()).isEqualTo(42L);
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.review()).isEqualTo("review_test");
    }

    @Test
    void toBookFullInfoResponse_mapsAverageRatingReviewsAndBook() {
        // given
        var book = new BookResponse(1L, "Moby Dick", List.of(new AuthorResponse(1819, 1891, "Melville, Herman")), List.of("en"), 120000);
        var reviews = List.of("review_test_1", "review_test_2");
        var averageRating = new BigDecimal("4.5");

        // when
        var result = mapper.toBookFullInfoResponse(averageRating, reviews, book);

        // then
        assertThat(result.book()).isEqualTo(book);
        assertThat(result.reviews()).containsExactly("review_test_1", "review_test_2");
        assertThat(result.rating()).isEqualByComparingTo(new BigDecimal("4.5"));
    }

    @Test
    void toBookFullInfoResponseWithNoReviews_setsZeroRatingAndEmptyReviewsList() {
        // given
        var book = new BookResponse(1L, "Moby Dick", List.of(new AuthorResponse(1819, 1891, "Melville, Herman")), List.of("en"), 120000);

        // when
        var result = mapper.toBookFullInfoResponseWithNoReviews(book);

        // then
        assertThat(result.book()).isEqualTo(book);
        assertThat(result.reviews()).isEmpty();
        assertThat(result.rating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

}
