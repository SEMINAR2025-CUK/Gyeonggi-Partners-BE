# 프로젝트 : 경기 파트너스

## 기술 스택

### 백엔드
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.6
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA
- **Migration**: FlywayMigration
- **Real-time**: WebSocket
- **Build Tool**: Gradle (Kotlin DSL)
- **API Docs**: SpringDoc OpenAPI (Swagger)

### DevOps
- **Containerization**: Docker
- **CI/CD**: GitHub Actions
- **Cloud**: AWS
- **Monitoring**: Spring Actuator, AWS CloudWatch

### Development
- **IDE**: IntelliJ IDEA
- **Logging**: Logback (SLF4J)
- **Code Quality**: Lombok



## 환경별 설정

프로젝트는 4개의 환경 설정 파일로 구성되어 있습니다:


| 파일 | 용도 | Git 포함 |
|------|------|----------|
| `application.properties` | 공통 설정 | ✅ |
| `application-local.properties` | 로컬 개발 환경 | ❌ (.gitignore) |
| `application-dev.properties` | 개발 서버 환경 | ✅ |
| `application-prod.properties` | 운영 서버 환경 | ✅ |


> 💡 `application-local.properties`는 각자 로컬에서 생성해야 합니다.











## 로컬 환경 세팅

[백엔드 로컬 환경 세팅 가이드](SETUP.md)를 참고하세요.





















