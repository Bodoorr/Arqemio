FROM eclipse-temurin:17-jdk-jammy

#create temporary drive
VOLUME /tmp

EXPOSE 8081

#create app dir
RUN mkdir -p /app/

#create logs dir
RUN mkdir -p /app/logs/

#cp demo-0.0.1-SNAPSHOT.jar /app/application.jar
ADD target/auth-service-0.0.1-SNAPSHOT.jar /app/app.jar

#run the .jar file with the active profile dev
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]