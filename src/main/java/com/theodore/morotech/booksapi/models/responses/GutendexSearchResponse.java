package com.theodore.morotech.booksapi.models.responses;

import java.util.List;

public record GutendexSearchResponse(int count, String next, String previous, List<BookResponse> results) {
}
