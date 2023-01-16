FROM adoptopenjdk:11-jre-hotspot
ARG APP_DIR=/app

WORKDIR ${APP_DIR}
COPY target ${APP_DIR}

CMD java -Xmx64m \
        -jar tokenService.jar