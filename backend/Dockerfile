# ========================================
# Stage: Runtime Only (for Jenkins Deploy)
# ========================================
FROM eclipse-temurin:21-jre-alpine

# 보안 및 최적화를 위한 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Jenkins가 backend/app.jar 을 미리 빌드 후 복사함
COPY app.jar /app/app.jar

# 소유권 변경
RUN chown -R spring:spring /app
USER spring

# JVM 최적화 옵션 (메모리 효율화)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# 헬스체크 (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
