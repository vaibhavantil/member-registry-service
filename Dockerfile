FROM openjdk:8


ADD target/must-rename-0.0.1-SNAPSHOT.jar /

ENTRYPOINT java -jar must-rename-0.0.1-SNAPSHOT.jar
