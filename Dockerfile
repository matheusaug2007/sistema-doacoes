# Etapa 1: compila e empacota. Os testes rodam no CI, não aqui.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
COPY database database
RUN mvn -B -q package -DskipTests

# Etapa 2: imagem final só com o JRE e o jar, rodando sem privilégios de root.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home app
COPY --from=build /app/target/sistema-doacoes-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
