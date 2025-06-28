FROM openjdk:17-jdk

# 1) RDS CA 다운로드 → 바로 JDK truststore에만 등록
RUN set -eux && \
    mkdir -p /tmp/certs && \
    curl -sSL https://truststore.pki.rds.amazonaws.com/ap-northeast-2/2019/rds-ca-rsa2048-g1.pem \
         -o /tmp/certs/rds-ca.pem && \
    keytool -importcert -trustcacerts \
         -alias rds-ca \
         -file /tmp/certs/rds-ca.pem \
         -keystore "${JAVA_HOME}/lib/security/cacerts" \
         -storepass changeit -noprompt && \
    rm -rf /tmp/certs

# 2) 애플리케이션 JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
