FROM amazoncorretto:11

ADD server_cert.pem /
RUN keytool -import -alias bankid -file /server_cert.pem -cacerts -storePass changeit -noprompt

RUN curl -o dd-java-agent.jar -L 'https://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.datadoghq&a=dd-java-agent&v=LATEST'

ADD member-service/target/member-service-0.0.1-SNAPSHOT.jar /
ENTRYPOINT java -javaagent:/dd-java-agent.jar -jar member-service-0.0.1-SNAPSHOT.jar
