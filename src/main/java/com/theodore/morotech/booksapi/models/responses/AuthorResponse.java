package com.theodore.morotech.booksapi.models.responses;

import java.io.Serializable;

public record AuthorResponse(Integer birthYear,
                             Integer deathYear,
                             String name) implements Serializable {
}
