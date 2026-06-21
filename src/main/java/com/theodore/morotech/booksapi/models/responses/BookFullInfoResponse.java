package com.theodore.morotech.booksapi.models.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record BookFullInfoResponse(@JsonUnwrapped BookResponse book,
                                   List<String> reviews,
                                   BigDecimal rating) implements Serializable {
}
