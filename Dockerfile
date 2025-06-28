# ---------- Dockerfile ----------
FROM openjdk:17-jdk

# 1) RDS CA 번들을 내려받아 시스템 truststore( /etc/ssl/certs )에 추가
RUN curl -sSL https://truststore.pki.rds.amazonaws.com/ap-northeast-2/2019/rds-ca-rsa2048-g1.pem \
      -o /usr/local/share/ca-certificates/rds-ca.pem \
 && update-ca-certificates

# 2) 동일한 CA를 JDK 내부 cacerts에도 등록
RUN keytool -import -trustcacerts -alias rds-ca \
      -file /usr/local/share/ca-certificates/rds-ca.pem \
      -keystore "${JAVA_HOME}/lib/security/cacerts" \
      -storepass changeit -noprompt

# 3) 애플리케이션 JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
# ---------------------------------
