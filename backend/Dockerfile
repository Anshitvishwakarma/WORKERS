
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*.jar --server.port=${PORT:-8080}"]
