package com.theodore.morotech.booksapi.models.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookReviewRequest(@NotNull
                                Long bookId,
                                @Min(1) @Max(5)
                                int rating,
                                @NotBlank
                                String review) {
}
