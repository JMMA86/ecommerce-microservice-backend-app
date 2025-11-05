
FROM eclipse-temurin:11-jre
ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
COPY target/*.jar user-service.jar
EXPOSE 8700
ENTRYPOINT ["java", "-jar", "user-service.jar"]


