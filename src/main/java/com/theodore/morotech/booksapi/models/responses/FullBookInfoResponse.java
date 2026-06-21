package com.theodore.morotech.booksapi.models.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;
import java.util.List;

public record FullBookInfoResponse(@JsonUnwrapped BookResponse book,
                                   List<String> review,
                                   BigDecimal rating) {
}
