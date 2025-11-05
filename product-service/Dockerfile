
FROM eclipse-temurin:11-jre
ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
COPY target/*.jar product-service.jar
EXPOSE 8500
ENTRYPOINT ["java", "-jar", "product-service.jar"]


