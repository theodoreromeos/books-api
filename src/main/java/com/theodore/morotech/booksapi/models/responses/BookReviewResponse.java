package com.theodore.morotech.booksapi.models.responses;

public record BookReviewResponse(Long bookId, int rating, String review) {
}
