FROM openjdk:15-slim-buster
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ARG LOG4JCONF_LOCATION=.
COPY ${LOG4JCONF_LOCATION}/sbml4j_log4j2.xml /logConfig/sbml4j_log4js.xml
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dlog4j.configurationFile=/logConfig/sbml4j_log4js.xml", "-Xms128m", "-Xmx8G", "-cp","app:app/lib/*","org.sbml4j.Sbml4jApplication"]
