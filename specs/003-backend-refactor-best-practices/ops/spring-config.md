# Spring Config (Cloud-Agnostic)

Use these settings in `application-prod.properties` unless overridden by a cloud-specific file.

## Core (Spring Boot 4)
- `spring.threads.virtual.enabled=true`
- `spring.mvc.problemdetails.enabled=true`

## DataSource (Hikari)
- `spring.datasource.hikari.maximum-pool-size=5`
- `spring.datasource.hikari.minimum-idle=1`
- `spring.datasource.hikari.connection-timeout=20000`
- `spring.datasource.hikari.idle-timeout=300000`
- `spring.datasource.hikari.max-lifetime=1800000`

## JPA
- `spring.jpa.open-in-view=false`
- `spring.jpa.show-sql=false`
- `spring.jpa.properties.hibernate.jdbc.batch_size=20`
- `spring.jpa.properties.hibernate.order_inserts=true`
- `spring.jpa.properties.hibernate.order_updates=true`
- `spring.jpa.properties.hibernate.default_batch_fetch_size=20`

## Server
- `server.tomcat.threads.max=20`
- `server.tomcat.accept-count=50`
- `server.tomcat.max-connections=200`
- `server.tomcat.connection-timeout=5s`

## Actuator
- `management.endpoints.web.exposure.include=health,info`
- `management.endpoint.health.show-details=never`

## Logging (baseline; cloud logging uses WARN for frameworks)
- `logging.level.root=INFO`
- `logging.level.org.springframework=INFO`
- `logging.level.org.springframework.web=WARN`
- `logging.level.org.springframework.security=WARN`
- `logging.level.org.hibernate.SQL=OFF`
- `logging.level.org.hibernate.type.descriptor.sql=OFF`
