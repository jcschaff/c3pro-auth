FROM openjdk:8-jdk-alpine
MAINTAINER Jim Schaff <schaff@uchc.edu>
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
ENV JDBC_DRIVER_NAME=org.postgresql.Driver
ENV JDBC_URL=jdbc:postgresql://database:5432/spring
ENV JDBC_USER=schaff
#ENV JDBC_PASS=
#			"-Djdbc.pass=", 
ENTRYPOINT ["java", \
			"-Djava.security.egd=file:/dev/./urandom", \
			"-Djdbc.driverClassName=${JDBC_DRIVER_NAME}", \
			"-Djdbc.url=${JDBC_URL}", \
			"-Djdbc.user=${JDBC_USER}", \
			"-jar","/app.jar"]