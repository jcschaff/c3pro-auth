FROM maven:3.5-jdk-8-alpine as build
ADD . /app
WORKDIR /app
RUN mvn clean install

FROM openjdk:8-jdk-alpine
MAINTAINER Jim Schaff <schaff@uchc.edu>
ARG JAR_FILE=c3pro-auth-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/${JAR_FILE} /app.jar
EXPOSE 8888
VOLUME /cert
ENTRYPOINT ["java", \
			"-Djava.security.egd=file:/dev/./urandom", \
			"-jar","/app.jar"]