FROM openjdk:10-sid as thebuild

COPY . /build
WORKDIR /build

RUN ./mvnw clean install

FROM openjdk:10-jre-slim-sid
RUN mkdir /app
COPY --from=thebuild /build/member-service/target/member-service-0.0.1-SNAPSHOT.jar /app/member-service-0.0.1-SNAPSHOT.jar
COPY --from=thebuild /build/server_cert.pem /app/server_cert.pem

WORKDIR /app
RUN keytool -import -alias bankid -file /app/server_cert.pem -cacerts -storePass changeit -noprompt

ENTRYPOINT java -jar member-service-0.0.1-SNAPSHOT.jar
