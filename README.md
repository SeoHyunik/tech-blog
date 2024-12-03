### Tech Blog 자동화 시스템

이 프로젝트는 Google Drive API와 OpenAI를 사용해 Obsidian에 작성된 기술 관련 Markdown 파일을 주기적으로 스캔하여 블로그 포맷에 맞게 편집하고, WordPress API를 통해 자동으로 블로그에 게시하는 시스템입니다.

---

### 목차
- [프로젝트 소개](#프로젝트-소개)
- [시나리오](#시나리오)
- [기술 스택](#설치-방법)
- [API 기능](#db-테이블-및-스키마-설계)
- [우선순위 및 진행 순서](#우선순위-및-진행-순서)

---

### 프로젝트 소개

이 시스템은 Obsidian에 정리된 기술 관련 Markdown 파일을 Google Drive에 업로드한 후, 파일을 스캔하고 편집하여 WordPress 블로그에 자동으로 업로드합니다. 스프링 배치와 API를 통해 블로그 글을 주기적으로 관리하고, 이미지를 포함한 파일들을 WordPress에 업로드하여 손쉽게 블로그를 업데이트할 수 있도록 지원합니다. 해당 업로드 내용은 kiwijam.kr에서 확인할 수 있습니다.

---

### 시나리오

1. **기술 자료 업로드**  
   Obsidian에서 작성된 기술 관련 `md` 파일을 Google Drive에 업로드합니다.

2. **Google Drive 접근 및 파일 스캔**  
   주기적으로 Google Drive API를 호출하여 지정된 디렉터리의 `md` 파일을 스캔합니다.

3. **파일 편집 및 이미지 처리**  
   OpenAI를 이용해 `md` 파일을 블로그 형식에 맞게 수정하고, 이미지가 포함된 경우 별도의 `Attached File` 폴더에서 참조합니다.

4. **DB 메타데이터 저장**  
   편집된 파일의 메타데이터만 DB에 저장하며, 원본 파일은 Google Drive에 그대로 유지합니다.

5. **WordPress API를 통한 블로그 업로드**  
   마크업 형식으로 편집된 글과 이미지를 WordPress API를 통해 블로그에 게시합니다.

6. **스프링 배치로 주기적 실행**  
   전체 과정을 스프링 배치를 통해 주기적으로 실행하여 블로그를 자동 업데이트합니다.

---

### 기술 스택

- **언어 및 프레임워크**: Java 18, Spring Boot 3.3.5
- **SDK**: Amazon Corretto 21
- **데이터베이스**: MySQL
- **API 서비스**:
  - Google Drive API
  - OpenAI API
  - WordPress API

---

### API 기능(v1)

1. **Google Drive 파일 스캔**  
   - **GET /scan-files**: 특정 디렉터리를 스캔하여 `md` 파일 목록을 반환합니다.
   - **POST /upload-files**: 스캔한 `md` 파일의 정보를 DB에 insert합니다. 

2. **문서 편집 및 이미지 처리**  
   - **POST /edit-document**: 저장된 `md` 파일의 텍스트를 OpenAI API를 통해 정리하고 블로그 형식으로 수정합니다.
   - **GET /retrieve-image**: `Attached File` 폴더의 이미지를 블로그 업로드 시 사용할 수 있도록 반환합니다.

3. **DB 저장 및 관리**  
   - **POST /save-metadata**: 편집된 `md` 파일의 메타데이터를 DB에 저장합니다.

4. **WordPress 블로그 업로드**  
   - **POST /upload-blog**: 최종 편집된 글을 마크업 형식으로 변환해 WordPress API로 업로드합니다.

---

### 우선순위 및 진행 순서

1. **기본 Spring 프로젝트 구성**
2. **파일 스캔 및 편집 API 구현**
3. **DB 저장 및 데이터 관리 로직**
4. **WordPress 업로드 및 이미지 처리 로직 개발**
