package com.theodore.morotech.booksapi.models.responses;

import java.util.List;

public record BookSearchResponse(List<BookResponse> books, int page, int size,
                                 int totalElements, int totalPages) {
}
