FROM openjdk:11-jre-slim-stretch

ENV JAVA_OPTS "-noverify -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom "

#Set the name of the executable
ENV EXECUTABLE_JAR app.jar

#Set the location of application and logging configuration
ENV LOGBACK_FILE /usr/config/logback.xml
ENV CONFIG_FILE /usr/config/production.conf
ENV CONFIG_OPTS "-Dconfig.file=$CONFIG_FILE -Dlogback.configurationFile=$LOGBACK_FILE"

# Add the executable jar
ADD ./libs/*.jar app.jar

EXPOSE 80
# Launch the Application
ENTRYPOINT ["sh", "-c"]
CMD [ "exec java $JAVA_OPTS $CONFIG_OPTS -jar $EXECUTABLE_JAR"]
