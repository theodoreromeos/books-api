FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /workspace

COPY books-api ./books-api
RUN mvn -f books-api/pom.xml -B -Dmaven.test.skip=true clean package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /workspace/books-api/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]