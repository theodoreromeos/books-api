package com.theodore.morotech.booksapi.models.responses;

import java.math.BigDecimal;

public record MonthlyRatingResponse(String month, BigDecimal averageRating) {
}
