# ========================================
# Stage 1: Build Stage
# ========================================
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Gradle 캐싱 최적화: 의존성 먼저 다운로드
COPY Dito/build.gradle Dito/settings.gradle ./
COPY Dito/gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY Dito/ ./
RUN gradle clean build -x test --no-daemon

# ========================================
# Stage 2: Runtime Stage (경량화)
# ========================================
FROM eclipse-temurin:21-jre-alpine

# 보안 및 최적화를 위한 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 빌드된 JAR 파일만 복사 (경량화)
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 소유권 변경
RUN chown -R spring:spring /app

USER spring

# JVM 최적화 옵션 (메모리 효율화)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# 환경 변수를 통한 설정 주입
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
