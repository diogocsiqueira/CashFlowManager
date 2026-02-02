# -------- build stage --------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# copia o projeto
COPY . .

# garante permissão no gradlew (no linux do container isso importa)
RUN chmod +x ./gradlew

# gera o jar do Spring Boot
RUN ./gradlew clean bootJar -x test

# -------- runtime stage --------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Render injeta PORT, e seu app usa ${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
