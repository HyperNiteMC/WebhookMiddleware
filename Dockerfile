FROM openjdk:11.0.8-jdk-buster

COPY webhook.jar .

VOLUME /settings.json

EXPOSE 8080

CMD [ "java", "-jar", "-Dfile.encoding=UTF-8", "webhook.jar" ]
