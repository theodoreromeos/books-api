# Books API

A Spring Boot REST API for searching books, submitting reviews, and querying rating statistics. Book data is sourced
from the public [Gutendex](https://gutendex.com) API and cached in Redis to minimise external calls. Reviews are
persisted in PostgreSQL.

---

## Tech Stack

| Technology                   | Version | Why                                                                  |
|------------------------------|---------|----------------------------------------------------------------------|
| Java                         | 25      | Virtual threads for efficient parallel page fetching from Gutendex   |
| Spring Boot                  | 4.1.0   | Auto-configuration and production-ready defaults                     |
| Spring Data JPA + PostgreSQL | -       | Persistent storage for book reviews                                  |
| Liquibase                    | -       | Version-controlled, reproducible schema migrations                   |
| Spring Cache + Redis         | 7       | Distributed cache with 10-minute TTL avoids redundant Gutendex calls |
| MapStruct                    | 1.6.3   | Compile-time, reflection-free object mapping                         |
| SpringDoc OpenAPI            | 3.0.3   | Auto-generated interactive API documentation (Swagger UI)            |
| Resilience4j                 | 2.4.0   | Configurable retry with exponential back-off on Gutendex failures    |
| Spring REST Client           | -       | Synchronous HTTP client for outbound Gutendex requests               |

---

## Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose

No local Java or Maven installation is required, the build runs inside Docker.

---

## Running the Application

The entire stack (API, PostgreSQL, Redis) is brought up with a single command from the **project root**:

```bash
1) git clone https://github.com/theodoreromeos/books-api.git
```

```bash
2) cd books-api
```

```bash
3) docker compose -f books-api/docker-compose.yaml up --build -d
```

To stop and remove containers:

```bash
docker compose -f books-api/docker-compose.yaml down
```

To tail the application logs:

```bash
docker logs -f books-api-service
```

### What gets started

| Container           | Port   | Description                 |
|---------------------|--------|-----------------------------|
| `books-api-service` | `8081` | The Spring Boot application |
| `books-db`          | `2345` | PostgreSQL 16 database      |
| `redis-cache`       | `6379` | Redis 7 cache               |

The application depends on both database and cache being healthy before it starts. Liquibase runs migrations
automatically on startup.

---

## API Documentation

Interactive Swagger UI is available once the application is running:

```
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON spec:

```
http://localhost:8081/v3/api-docs
```

---

## Endpoints

Base URL: `http://localhost:8081`

---

### Search books by title

```
GET /api/books/search
```

Searches the Gutendex catalogue by title. Results are filtered server-side to match all terms and paginated. The full
result set for a given title is cached in Redis.

**Query parameters**

| Parameter | Required | Default | Constraints |
|-----------|----------|---------|-------------|
| `title`   | yes      | no      | non-blank   |
| `page`    | no       | `0`     | ≥ 0         |
| `size`    | no       | `20`    | 5 to 100    |

**Example**

```bash
curl "http://localhost:8081/api/books/search?title=moby&page=0&size=5"
```

**Response**

```json
{
  "books": [
    {
      "id": 2701,
      "title": "Moby Dick; Or, The Whale",
      "authors": [
        {
          "name": "Melville, Herman",
          "birth_year": 1819,
          "death_year": 1891
        }
      ],
      "languages": [
        "en"
      ],
      "download_count": 120000
    }
  ],
  "page": 0,
  "size": 5,
  "total_elements": 6,
  "total_pages": 2
}
```

---

### Submit a book review

```
POST /api/books/review
```

Creates a review for a book. The book must exist in the Gutendex catalogue. A `404` is returned otherwise.

**Request body**

```json
{
  "book_id": 2701,
  "rating": 5,
  "review": "Great stuff"
}
```

| Field     | Type   | Constraints |
|-----------|--------|-------------|
| `book_id` | Long   | required    |
| `rating`  | int    | 1 to 5      |
| `review`  | String | non-blank   |

**Example**

```bash
curl -X POST http://localhost:8081/api/books/review \
  -H "Content-Type: application/json" \
  -d '{"book_id": 2701, "rating": 5, "review": "Great stuff"}'
```

**Response**

```json
{
  "book_id": 2701,
  "rating": 5,
  "review": "Great stuff"
}
```

---

### Get full book info with reviews

```
GET /api/books/review/{id}
```

Returns book metadata together with all submitted reviews and their computed average rating. Returns a `404` if the book
does not exist in Gutendex.

**Example**

```bash
curl http://localhost:8081/api/books/review/2701
```

**Response**

```json
{
  "id": 2701,
  "title": "Moby Dick; Or, The Whale",
  "authors": [
    {
      "name": "Melville, Herman",
      "birth_year": 1819,
      "death_year": 1891
    }
  ],
  "languages": [
    "en"
  ],
  "download_count": 120000,
  "reviews": [
    "Great stuff",
    "I cannot read"
  ],
  "rating": 4.50
}
```

---

### Get top N books by average rating

```
GET /api/books/top/{count}
```

Returns the top N books ranked by their average review rating, highest first. Only books that have at least one review
appear in the list.

**Path parameter**

| Parameter | Constraints |
|-----------|-------------|
| `count`   | 1 to 20     |

**Example**

```bash
curl http://localhost:8081/api/books/top/5
```

**Response**

```json
[
  {
    "id": 2701,
    "title": "Moby Dick; Or, The Whale",
    "reviews": [
      "I am allergic to fish and whales"
    ],
    "rating": 3.00
  }
]
```

---

### Get monthly average rating for a book

```
GET /api/books/ratings/monthly/{id}
```

Returns the average rating per calendar month for a given book, ordered chronologically. Months with no reviews are
omitted.

**Example**

```bash
curl http://localhost:8081/api/books/ratings/monthly/2701
```

**Response**

```json
[
  {
    "month": "2026-05",
    "average_rating": 4.00
  },
  {
    "month": "2026-06",
    "average_rating": 5.00
  }
]
```

---

## Health & Observability

Spring Boot Actuator is available at:

```
http://localhost:8081/actuator
```

Key endpoints:

```
http://localhost:8081/actuator/health
http://localhost:8081/actuator/info
```

---

## Running Tests

Unit tests only:

```bash
mvn test
```

Unit + integration tests (requires Docker for Testcontainers):

```bash
mvn verify -P integration
```

# Architecture Choices in order to scale the project

| Component         | Choice                                                   | Reason                                                                                    |
|-------------------|----------------------------------------------------------|-------------------------------------------------------------------------------------------|
| **Orchestration** | Kubernetes + HPA                                         | Auto-scales pods based on CPU/RPS                                                         |
| **API Gateway**   | Ingress + rate limiting                                  | Protects the backend from spamming, ddos etc                                              |
| **Cache**         | Redis                                                    | Absorbs read-heavy traffic (book listings, ratings)                                       |
| **DB**            | Postgres now, MondoDB later for scale                    | Postgres now for simplicity, MongoDB later to scale because of high volume and read heavy |
| **CI/CD**         | GitHub Actions -> registry -> kubernetes rolling updates | Zero-downtime rolling deploys                                                             |
| **Observability** | Prometheus/Grafana/Loki                                  | Catch regressions before users do                                                         |
