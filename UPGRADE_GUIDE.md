# TMS Spring Boot 1.x 升级到 2.x 升级指南

## 概述

本文档记录了TMS项目从Spring Boot 1.5.14升级到Spring Boot 2.4.13的所有变更。

## 主要变更

### 1. pom.xml 依赖升级

#### Spring Boot 版本升级
- **变更前**: `1.5.14.RELEASE`
- **变更后**: `2.4.13`

#### 新增依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

#### 更新依赖版本
1. **Thymeleaf Security 依赖**
   - 变更前: `thymeleaf-extras-springsecurity4`
   - 变更后: `thymeleaf-extras-springsecurity5`

2. **OAuth2 依赖**
   - 变更前: `spring-security-oauth2`
   - 变更后: `spring-security-oauth2-autoconfigure:2.4.1`

3. **补充缺失的依赖版本**
   - `commons-beanutils:1.9.4`
   - `janino:3.1.7`
   - `nekohtml:1.9.22`
   - `joda-time:2.10.14`

### 2. 配置文件变更 (application.properties)

#### 数据源配置
```properties
# 变更前
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/tms?useUnicode=true&characterEncoding=UTF-8&useSSL=false

# 变更后
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/tms?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
```

#### 连接池配置 (从Tomcat切换到HikariCP)
```properties
# 变更前
spring.datasource.tomcat.*

# 变更后
spring.datasource.hikari.*
```

#### 其他配置项变更
```properties
# 变更前
server.context-path=/tms
spring.http.multipart.max-file-size=10Mb
spring.http.multipart.max-request-size=50Mb
logging.path=log/tms
logging.file=tms.log
management.context-path=/manage
spring.datasource.initialize=false
endpoints.enabled=false
#server.session.timeout=600

# 变更后
server.servlet.context-path=/tms
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
logging.file.name=log/tms/tms.log
management.endpoints.web.base-path=/manage
spring.sql.init.mode=never
management.endpoints.enabled-by-default=false
#server.servlet.session.timeout=600s
```

### 3. 代码变更

#### WebSocket 配置 (WsConfig.java)
- **变更**: 从继承 `AbstractWebSocketMessageBrokerConfigurer` 改为实现 `WebSocketMessageBrokerConfigurer` 接口
- **变更**: `setAllowedOrigins("*")` 改为 `setAllowedOriginPatterns("*")`

#### Servlet 初始化器 (ServletInitializer.java)
- **变更**: 包名从 `org.springframework.boot.web.support` 改为 `org.springframework.boot.web.servlet.support`

#### 日志注解变更
以下文件中的 `@Log4j` 注解改为 `@Slf4j`:
- [MailSender.java](file:///Users/xiweicheng/tms/src/main/java/com/lhjz/portal/component/MailSender.java)
- [AsyncTask.java](file:///Users/xiweicheng/tms/src/main/java/com/lhjz/portal/component/AsyncTask.java)
- [MailQueueImpl.java](file:///Users/xiweicheng/tms/src/main/java/com/lhjz/portal/component/core/impl/MailQueueImpl.java)

#### 模板工具类 (TemplateUtil.java)
- **变更**: Thymeleaf 3.x API 适配
  - 移除 `TemplateResolver` 抽象类，直接使用 `ClassLoaderTemplateResolver`
  - 模板模式从 `"LEGACYHTML5"` 改为 `TemplateMode.HTML`
  - 导入变更为 `org.thymeleaf.templatemode.TemplateMode`

## 已知问题

### 测试代码问题
测试代码中有一些API不兼容问题，主要是Spring Data JPA的`findOne`方法变更。如果需要运行测试，需要修复以下测试文件:
- [ChatAtRepositoryTest.java](file:///Users/xiweicheng/tms/src/test/java/com/lhjz/portal/repository/ChatAtRepositoryTest.java)
- [BlogRepositoryTest.java](file:///Users/xiweicheng/tms/src/test/java/com/lhjz/portal/repository/BlogRepositoryTest.java)
- [ChatDirectRepositoryTest.java](file:///Users/xiweicheng/tms/src/test/java/com/lhjz/portal/repository/ChatDirectRepositoryTest.java)

## 验证结果

✅ **编译成功**: 主代码编译通过  
✅ **构建成功**: 项目打包成功，生成了WAR文件  
⚠️ **测试代码**: 测试代码有API兼容性问题，需要单独修复

## 后续建议

1. **修复测试代码**: 更新测试代码以适配Spring Boot 2.x的新API
2. **功能测试**: 在测试环境中充分测试所有功能
3. **性能优化**: 利用Spring Boot 2.x的新特性进行性能优化
4. **安全性增强**: 检查并更新安全配置

## 参考资料

- [Spring Boot 2.x 迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide)
- [Spring Security 5.x 迁移指南](https://docs.spring.io/spring-security/site/docs/5.0.x/reference/htmlsingle/#migration)
