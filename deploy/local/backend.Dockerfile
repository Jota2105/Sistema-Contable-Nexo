FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend_api/pom.xml ./pom.xml
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY backend_api/src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

FROM payara/server-full:7.2026.5-jdk25
COPY --from=build /workspace/target/backend_api.war ${DEPLOY_DIR}/backend_api.war
