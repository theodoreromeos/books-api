package com.theodore.morotech.booksapi.mappers;

import com.theodore.morotech.booksapi.entities.BookReviews;
import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.BookFullInfoResponse;
import com.theodore.morotech.booksapi.models.responses.BookResponse;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    BookReviews requestToEntity(BookReviewRequest request);

    BookReviewResponse entityToResponse(BookReviews entity);

    @Mapping(target = "rating", source = "averageRating")
    @Mapping(target = "reviews", source = "reviews")
    @Mapping(target = "book", source = "book")
    BookFullInfoResponse toBookFullInfoResponse(BigDecimal averageRating, List<String> reviews, BookResponse book);

    @Mapping(target = "book", source = "book")
    @Mapping(target = "rating", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "reviews", expression = "java(java.util.List.of())")
    BookFullInfoResponse toBookFullInfoResponseWithNoReviews(BookResponse book);

}
