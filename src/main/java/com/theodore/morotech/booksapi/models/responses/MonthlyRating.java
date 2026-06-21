package com.theodore.morotech.booksapi.models.responses;

import java.math.BigDecimal;

public interface MonthlyRating {

    String getMonth();
    BigDecimal getAverageRating();

}
