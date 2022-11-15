FROM tomcat:8.5.69-jdk8-openjdk
WORKDIR /usr/local/tomcat
COPY target/fwd-is-customerprofile.war /usr/local/tomcat/webapps
RUN cp /usr/local/tomcat/webapps/fwd-is-customerprofile.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir /APPCONF
COPY properties/%ENVIRONMENT%/common.properties /APPCONF/
ENV CONFIG_DIR=/APPCONF/
EXPOSE 8080
USER root
CMD chmod +x /usr/local/tomcat/bin/catalina.sh
CMD ["catalina.sh", "run"]
