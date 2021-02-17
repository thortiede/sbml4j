FROM openjdk:11-jre-slim-buster
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ARG LOG4JCONF_LOCATION=.
COPY ${LOG4JCONF_LOCATION}/sbml4j_log4j2.xml /logConfig/sbml4j_log4js.xml
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dlog4j.configurationFile=/logConfig/sbml4j_log4js.xml","-cp","app:app/lib/*","org.tts.Sbml4jApplication"]
