# Compose profile

- state hoisting, lifecycle-aware collection, stable input과 기존 design system을 우선합니다.
- 성능 변경은 측정 가능한 병목과 재현 조건을 먼저 확보합니다.
- 사용자 노출 UI 변경은 preview와 accessibility를 확인하고, screenshot은 프로젝트가 채택한 local candidate 또는 regression 정책을 따릅니다. project 계약 없이 golden·CI gate를 가정하지 않습니다.
- Modifier 순서와 effect lifecycle을 동작 계약으로 취급합니다.
