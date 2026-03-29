# Spring Boot Buildpack - macOS 트러블슈팅

`./gradlew bootBuildImage` 실행 시 macOS(Apple Silicon) + Docker Desktop 환경에서 발생하는 문제 해결 기록

---

## 환경

- macOS Apple Silicon (ARM64)
- Docker Desktop (Use Rosetta for x86_64/amd64 emulation 활성화 상태)
- Spring Boot 3.5.x
- 기본 빌더: `paketobuildpacks/builder-noble-java-tiny`

---

## 시도 1: builder 명시 → 실패

### 배경

`bootBuildImage` 첫 실행 시 아래 에러 발생:

```
No 'io.buildpacks.builder.metadata' label found in image config labels ''
```

빌더 이미지가 명시되지 않아 생기는 문제로 판단하여 `build.gradle.kts`에 빌더를 명시적으로 지정:

```kotlin
tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-noble-java-tiny:latest")
}
```

### 결과: 동일한 에러 반복

```
No 'io.buildpacks.builder.metadata' label found in image config labels ''
```

---

## 시도 2: docker inspect로 원인 파악

builder를 명시해도 에러가 동일하게 발생했으므로, 실제로 이미지에 라벨이 있는지 확인:

```bash
docker inspect paketobuildpacks/builder-noble-java-tiny:latest | grep -A 10 '"Labels"'
```

### 결과: 라벨이 아예 없음

아무것도 출력되지 않음 → 이미지 자체에 라벨이 없는 상태.

### 원인 파악

Apple Silicon Mac에서 멀티 아키텍처 이미지를 pull하면 Docker는 기본적으로 ARM64 변형을 가져옴.
Spring Boot buildpack 라이브러리가 이미지 config를 읽을 때 플랫폼별 매니페스트가 아닌
OCI Image Index(멀티 아키텍처 매니페스트 목록)를 참조하면 라벨 필드가 비어 있어
CNB 메타데이터(`io.buildpacks.builder.metadata`)를 읽지 못함.

---

## 시도 3: imagePlatform 명시 → 에러 변경

`build.gradle.kts`에 플랫폼을 linux/amd64로 명시:

```kotlin
tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-noble-java-tiny:latest")
    imagePlatform.set("linux/amd64")
}
```

> `imagePlatform`은 Spring Boot 3.4.0+에서 지원. 빌더 이미지 pull 플랫폼도 함께 제어함.

### 결과: 에러가 바뀜 (진전)

```
ERROR: failed to export: saving image: failed to fetch base layers:
saving image with ID "sha256:1625704..." from the docker daemon:
Error response from daemon: unable to create manifests file:
NotFound: content digest sha256:7ef2934...: not found

Builder lifecycle 'creator' failed with status code 62
```

EXPORTING 단계까지 진행됐으므로 빌드 자체는 성공. 최종 이미지를 Docker에 저장하는 단계에서 실패.

---

## 시도 4: Docker 이미지 캐시 정리 → 실패

content digest가 손상된 캐시 때문일 수 있어 Paketo 이미지 전체 삭제 후 재시도:

```bash
docker images | grep paketobuildpacks | awk '{print $3}' | xargs docker rmi -f
docker builder prune -f
./gradlew bootBuildImage
```

### 결과: 동일한 에러 반복

동일한 sha256 digest에서 동일하게 실패 → 캐시 문제가 아님.

---

## 시도 5: containerd 이미지 스토어 비활성화 → 성공

### 원인 파악

Docker Desktop의 **"Use containerd for pulling and storing images"** 옵션이 활성화된 경우,
이미지 레이어를 containerd 방식으로 저장함.

CNB의 `creator` lifecycle은 빌드 완료 후 Docker 소켓 API로 직접 베이스 레이어를 읽어
최종 이미지를 조립하는데, containerd 스토어 방식과 충돌하여 content digest를 찾지 못함:

```
빌드 완료 (EXPORTING 단계)
    ↓
Docker 소켓 API로 베이스 레이어 조회
    ↓
containerd 스토어에서 digest 불일치 → NotFound 에러
```

### 해결

Docker Desktop → Settings → General → **"Use containerd for pulling and storing images" 체크 해제** → Apply & Restart

### 결과: 빌드 성공

---

## 최종 설정 요약

### Docker Desktop Settings

| 설정 | 값 |
|------|----|
| Use Rosetta for x86_64/amd64 emulation on Apple Silicon | ✅ 활성화 |
| Use containerd for pulling and storing images | ☐ 비활성화 |

### build.gradle.kts

```kotlin
tasks.bootBuildImage {
    imageName.set("${project.name}:${project.version}")
    builder.set("paketobuildpacks/builder-noble-java-tiny:latest")
    imagePlatform.set("linux/amd64")
    environment.set(mapOf("BP_JVM_VERSION" to "17"))
}
```

---

## 트러블슈팅 흐름 요약

```
bootBuildImage 실패
    │
    ├─ 시도 1: builder 명시                     → 동일 에러
    ├─ 시도 2: docker inspect으로 라벨 확인      → 라벨 없음 확인, 원인 파악
    ├─ 시도 3: imagePlatform("linux/amd64") 추가 → 다른 에러로 진전
    ├─ 시도 4: Docker 이미지 캐시 정리           → 동일 에러
    └─ 시도 5: containerd 이미지 스토어 비활성화 → 성공 ✅
```

---

## 참고

- [Spring Boot - bootBuildImage imagePlatform](https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html)
- [Paketo Buildpacks 공식 문서](https://paketo.io/docs/)
- [Cloud Native Buildpacks lifecycle](https://buildpacks.io/docs/concepts/components/lifecycle/)
