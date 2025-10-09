# 로컬 환경 세팅 가이드

## 사전 요구사항

다음 프로그램들이 설치되어 있어야 합니다:

- **Java 21** (JDK)
- **Docker Desktop** (PostgreSQL 실행용)
- **IntelliJ IDEA** (권장)
- **Git**

## 1. 환경 설정 파일 생성

> ⚠️ **중요**: `application-local.properties`는 개인 로컬 환경 설정 파일로, `.gitignore`에 포함되어 Git에 올라가지 않습니다. 따라서 **반드시 직접 생성**해야 합니다.

## 2. 프로젝트 클론 

> git clone "https://github.com/NiceLeeMan/gyeonggi_partners"

## 3. 도커 실행하기
- **프로젝트 루트로 이동**: cd gyeonggi_partners 
- **도커 컨테이너 실행**: docker-compose up -d
- **컨테이너 실행 확인**: docker-compose ps
- **컨테이너 완전 종료**: docker-compose down -v


## 추가 정보

- [README.md](../../README.md) - 프로젝트 개요
- [백엔드 아키텍처](README.md) - 아키텍처 설명
