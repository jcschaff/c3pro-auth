FROM openjdk:8-jdk-alpine
MAINTAINER Jim Schaff <schaff@uchc.edu>
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
ENV JDBC_DRIVER_NAME=org.postgresql.Driver
ENV JDBC_URL=jdbc:postgresql://database:5432/docker
ENV JDBC_USER=docker
ENV JDBC_PASS=docker
EXPOSE 8081
ENTRYPOINT ["java", \
			"-Djava.security.egd=file:/dev/./urandom", \
			"-Djdbc.driverClassName=${JDBC_DRIVER_NAME}", \
			"-Djdbc.url=${JDBC_URL}", \
			"-Djdbc.user=${JDBC_USER}", \
			"-Djdbc.pass=${JDBC_PASS}", \
			"-jar","/app.jar"]