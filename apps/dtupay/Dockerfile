FROM adoptopenjdk:11-jre-hotspot
ARG APP_DIR=/app

WORKDIR ${APP_DIR}
COPY target/quarkus-app ${APP_DIR}/quarkus-app

CMD java -Xmx64m \
        -jar quarkus-app/quarkus-run.jar



