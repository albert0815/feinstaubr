FROM jboss/wildfly:11.0.0.Final

ADD ./docker/modules /opt/jboss/wildfly/modules
ADD ./docker/standalone-custom.xml /opt/jboss/wildfly/standalone/configuration/
ADD ./target/feinstaubr-web-*.war /opt/jboss/wildfly/standalone/deployments/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-custom.xml", "-b", "0.0.0.0"]
