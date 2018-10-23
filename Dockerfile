FROM java:8
COPY target/*.jar app.jar
COPY docker/start.sh start.sh
COPY application.properties application.properties
RUN chmod +x start.sh
CMD ["./start.sh"]