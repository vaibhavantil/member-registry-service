FROM openjdk:8


ADD member-service/target/member-service-0.0.1-SNAPSHOT.jar /

ENTRYPOINT java -jar member-service-0.0.1-SNAPSHOT.jar
