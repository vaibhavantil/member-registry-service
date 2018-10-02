FROM openjdk:10.0.2-13-jdk-slim-sid


ADD server_cert.pem /
RUN keytool -import -alias bankid -file /server_cert.pem -cacerts -storePass changeit -noprompt

ADD member-service/target/member-service-0.0.1-SNAPSHOT.jar /
ENTRYPOINT java -jar member-service-0.0.1-SNAPSHOT.jar
