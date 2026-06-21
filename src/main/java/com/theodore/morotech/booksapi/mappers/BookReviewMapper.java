package com.theodore.morotech.booksapi.mappers;

import com.theodore.morotech.booksapi.entities.BookReviews;
import com.theodore.morotech.booksapi.models.requests.BookReviewRequest;
import com.theodore.morotech.booksapi.models.responses.BookReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    BookReviews requestToEntity(BookReviewRequest request);

    BookReviewResponse entityToResponse(BookReviews entity);

}
