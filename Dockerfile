
##### Dependencies stage #####
FROM maven:3.6.3-amazoncorretto-11 AS local_dependencies
WORKDIR /usr/app

# Running `mvn dependency:go-offline` and similar will not be able to detect
# local module dependencies that have not yet been built. In order to make sure
# we can split up the different build stages, we build+install the local deps
# upfront, to then only focus on the actual application module: member-service.
COPY pom.xml .
COPY bank-id bank-id
RUN mvn install -f bank-id/pom.xml -s /usr/share/maven/ref/settings-docker.xml
COPY bisnode-bci bisnode-bci
RUN mvn install -f bisnode-bci/pom.xml -s /usr/share/maven/ref/settings-docker.xml
COPY syna syna
RUN mvn install -f syna/pom.xml -s /usr/share/maven/ref/settings-docker.xml
COPY zign-sec zign-sec
RUN mvn install -f zign-sec/pom.xml -s /usr/share/maven/ref/settings-docker.xml


##### Dependencies stage #####
FROM local_dependencies AS dependencies

# Resolve dependencies and cache them
COPY member-service/pom.xml member-service/
RUN mvn dependency:go-offline -pl member-service -s /usr/share/maven/ref/settings-docker.xml


##### Build stage #####
FROM dependencies AS build

COPY member-service/src/main member-service/src/main
COPY member-service/lombok.config member-service/
RUN mvn clean package -pl member-service -s /usr/share/maven/ref/settings-docker.xml


##### Test stage #####
FROM build AS test
COPY member-service/src/test member-service/src/test
RUN mvn test -pl member-service -s /usr/share/maven/ref/settings-docker.xml


##### Assemble stage #####
FROM amazoncorretto:11 AS assemble

ENV LANG C.UTF-8

ADD server_cert.pem /
RUN keytool -import -alias bankid -file /server_cert.pem -cacerts -storePass changeit -noprompt

RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

# Copy the jar from build stage to this one
COPY --from=build /usr/app/member-service/target/member-service-0.0.1-SNAPSHOT.jar .

ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar member-service-0.0.1-SNAPSHOT.jar -XX:-OmitStackTraceInFastThrow
