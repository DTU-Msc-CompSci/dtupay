FROM adoptopenjdk:11-jre-hotspot
ARG APP_DIR=/app

WORKDIR ${APP_DIR}
COPY target ${APP_DIR}

# COPY target/accountService.jar ${APP_DIR}/accountService.jar

CMD java -Xmx64m \
        -jar accountService.jar


