
FROM eclipse-temurin:11-jre
ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
COPY target/*.jar shipping-service.jar
EXPOSE 8600
ENTRYPOINT ["java", "-jar", "shipping-service.jar"]


