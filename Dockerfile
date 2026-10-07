# --- Etapa 1: compilación con Maven ---------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom: si no cambian las dependencias, Docker reutiliza esta capa
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# --- Etapa 2: imagen final, solo con el JRE y el jar ------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# No ejecutar la aplicación como root
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /app/target/api-gestion-incidencias-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
