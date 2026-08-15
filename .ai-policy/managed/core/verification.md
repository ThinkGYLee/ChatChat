# 공통 완료와 검증

- 변경 위험에 비례한 test, build, lint 또는 정적 검증을 실행합니다.
- 실행하지 못한 검증과 이유를 명확히 기록합니다.
- 최종 응답에는 변경 파일, 변경 요약, 검증 결과, 남은 위험을 포함합니다.
- generated output은 동일 입력에서 byte-for-byte 동일해야 합니다.
- 배포 전 schema, 허용 경로, base revision, digest를 trusted validator가 확인해야 합니다.
