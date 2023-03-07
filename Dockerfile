
FROM openjdk:11.0.11-jre-slim
#COPY instruction is to copy the files or directories from source to destination
COPY target/%APP_NAME%-%VERSION%.jar .
# RUN instruction runs the commads written
RUN mkdir -p /home/localfiles
RUN ["chmod", "+x", "/usr/local/openjdk-11"]
#CMD chmod -R 755 /usr/local/openjdk-11
USER root
ENV TZ="Asia/Singapore"
CMD java -Xmx400m -Xms400m -jar %APP_NAME%-%VERSION%.jar
# which port will be used , for example in server.xml connector port is 8085
EXPOSE 5002