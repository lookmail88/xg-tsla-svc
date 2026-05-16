FROM eclipse-temurin:25
# 1. 统一创建特定 UID 的用户 (假设你需要 70501)
RUN groupadd -g 70501 spring && \
   useradd -u 70501 -g spring -m spring
# 2. 设置时区环境变量
ENV TZ=America/Los_Angeles
# 3. 准备目录并处理权限 (在切换用户前完成)
WORKDIR /app
RUN mkdir -p /tmp && \
   chown -R spring:spring /app /tmp
# 4. 复制文件并确保所有权属于 spring 用户
ARG JAR_FILE
COPY --chown=spring:spring target/*.jar /app/app.jar
# 5. 切换到非 root 用户
USER spring
# 6. 声明挂载点
VOLUME /tmp
# 7. 启动命令（最后执行）
ENTRYPOINT ["java", "-Duser.timezone=America/Los_Angeles", "-jar", "/app/app.jar"]