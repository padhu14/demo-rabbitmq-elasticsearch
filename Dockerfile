#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean install

#
# Package stage
#
FROM openjdk:11-jre-slim
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
COPY --from=build /home/app/target/Guice-rabbitMQ-1.0-SNAPSHOT.jar  /usr/local/lib/demo.jar
EXPOSE 8080:8080
CMD ["java","-jar","/usr/local/lib/demo.jar"]


HEALTHCHECK --interval=1m --timeout=3s CMD wget --quiet --tries=1 --spider http://localhost:8081/ || exit 1