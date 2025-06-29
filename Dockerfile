FROM openjdk:17-jdk

# RDS CA 다운로드 → JDK truststore만 등록
RUN set -eux && \
    mkdir -p /tmp/certs && \
    curl -sSLo /tmp/certs/rds-ca.pem \
      https://truststore.pki.rds.amazonaws.com/ap-northeast-2/2019/rds-ca-rsa2048-g1.pem && \
    keytool -importcert -trustcacerts -alias rds-ca \
      -file /tmp/certs/rds-ca.pem \
      -keystore "$JAVA_HOME/lib/security/cacerts" \
      -storepass changeit -noprompt && \
    rm -rf /tmp/certs

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
