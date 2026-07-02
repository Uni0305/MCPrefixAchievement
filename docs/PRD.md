# MCPrefixAchievement — Product Requirements Document

> **문서 버전**: 1.0  
> **작성일**: 2026-07-03  
> **대상 독자**: 개발자  
> **배포 형태**: 단일 서버 전용 (Paper 1.21.x, Java 21)

---

## 1. Executive Summary

### 1.1 Problem Statement

단일 마인크래프트 서버 운영자가 플레이어의 다양한 행위(사망, 블럭 파괴/설치, 보스 처치 등)를 **칭호(Prefix)** 라는 보상 체계로 전환할 수 있는 시스템이 부재하다. 기존 업적 시스템은 통계 추적·달성 검사·보상 지급·칭호 표시가 단절되어 있거나, 운영자가 수동으로 관리해야 하는 비효율이 있다.

### 1.2 Proposed Solution

Paper API 1.21.x 기반의 **독립 구동 플러그인**으로, (1) 플레이어 행위 통계를 MySQL에 영속 저장하고, (2) `prefix.yml`에 정의된 달성 조건을 임계값 기반으로 자동 검사하며, (3) 달성 시 **하드코딩된 보상**(아이템/효과/명령어)을 자동 지급하고, (4) 획득한 칭호를 채팅·탭리스트·네임택에 표시·장착할 수 있게 한다. 보상 지급 로직은 `prefix.yml`의 reward 필드(표시 전용)와 분리하여 Java 코드에 하드코딩한다.

### 1.3 Success Criteria

| # | KPI | 측정 기준 |
|---|-----|-----------|
| 1 | 통계 정확도 | 11종 `PrefixStat` 이벤트가 누락/중복 없이 100% 기록 (오차 0) |
| 2 | 달성 반응성 | 조건 충족 후 보상 지급까지 지연 ≤ 1초 (비동기 DB I/O 포함) |
| 3 | 표시 일관성 | 채팅·탭리스트·네임택 3채널에서 장착 칭호가 동시에 일치 표시 |
| 4 | DB 내결함성 | 동시 다중 플레이어(≥50명) 환경에서 통계 갱신 시 데이터 정합성 보장 (트랜잭션/UPSERT) |
| 5 | 단독 구동 | 외부 플러그인 0개 의존, Paper API만으로 전 기능 동작 |

---

## 2. 사용자 경험 및 기능 (User Experience & Functionality)

### 2.1 사용자 페르소나

- **플레이어**: 서버 참여자. 자신의 행위로 칭호를 획득하고, 획득한 칭호 중 1개를 장착하여 타인에게 과시하고자 한다.
- **운영자(관리자)**: `prefix.yml`/`config.yml`을 편집해 칭호를 정의·운영하며, 플러그인 리로드, 수동 지급/회수, 통계 초기화 등을 수행한다.

### 2.2 사용자 스토리 및 수용 기준

#### Story P-1: 칭호 정보 확인
> 플레이어로서, 특정 칭호의 달성 조건과 보상을 확인하고 싶다.

**수용 기준**
- `/칭호 정보 <ID>` 실행 시 display-name, 달성 조건(description), 보상(reward)이 MiniMessage 포맷으로 출력된다.
- 존재하지 않는 ID 입력 시 `<red>올바르지 않은 칭호 ID입니다.` 안내.
- **현재 구현됨** (`PrefixCommand.java:36-58`)

#### Story P-2: 칭호 목록 조회
> 플레이어로서, 전체 칭호 목록을 페이지 단위로 탐색하고 싶다.

**수용 기준**
- `/칭호 목록 [페이지]` 실행 시 15개씩 ID 오름차순 정렬 출력.
- 범위 초과 페이지 입력 시 안내 메시지.
- **현재 구현됨** (`PrefixCommand.java:59-93`)

#### Story P-3: 통계 자동 추적
> 플레이어로서, 내 행위(사망/블럭 파괴 등)가 자동으로 집계되기를 원한다.

**수용 기준**
- 11종 `PrefixStat` 각각 대응하는 Bukkit 이벤트가 발생 시 MySQL 통계 값이 갱신된다.
- 서버 재시작 후에도 통계가 유지된다(영속성).
- **미구현** — 섹션 3-4 참조.

#### Story P-4: 칭호 자동 달성 + 보상 지급
> 플레이어로서, 조건 충족 시 칭호와 보상이 자동 지급되기를 원한다.

**수용 기준**
- 통계 갱신 후 `required-stat`/`required-stat-value` 충족 여부 검사.
- 미달성 → 달성 전환 시 즉시 **하드코딩된 보상**(아이템/효과/명령어) 자동 지급 (`RewardProvider` 기반).
- `prefix.yml`의 reward 필드는 표시 전용이며, 실제 지급은 `RewardProvider` 하드코딩 로직을 따른다.
- 이미 달성한 칭호는 중복 지급하지 않는다(`player_prefixes` 테이블로 보장).
- **미구현** — 섹션 3-5 참조.

#### Story P-5: 칭호 장착 및 표시
> 플레이어로서, 획득한 칭호 중 1개를 장착해 채팅·탭·네임택에 표시하고 싶다.

**수용 기준**
- 한 번에 1개만 장착 가능. 다른 칭호 장착 시 기존 것 자동 해제.
- 채팅·탭리스트·네임택 3채널 동시 일치 표시. 채널별 `config.yml` on/off 가능.
- 미획득 칭호는 장착 불가.
- **미구현** — 섹션 3-6 참조.

#### Story A-1: 운영자 관리 명령
> 운영자로서, 칭호/통계를 수동 관리하고 싶다.

**수용 기준**
- `/칭호 리로드` — `prefix.yml`/`config.yml` 재적재.
- `/칭호 지급 <플레이어> <ID>` — 칭호 수동 지급.
- `/칭호 회수 <플레이어> <ID>` — 칭호 회수.
- `/칭호 초기화 <플레이어> [통계]` — 통계 초기화(옵션).
- `/칭호 조회 <플레이어>` — 플레이어 보유 칭호/통계 조회.
- 권한: `mcprefixachievement.admin.*`
- **미구현** — 섹션 3-7 참조.

### 2.3 Non-Goals (명시적 제외)

아래 기능은 본 PRD 범위에서 **명시적으로 제외**한다.

| # | 제외 기능 | 제외 사유 |
|---|-----------|-----------|
| 1 | 웹 대시보드/관리 UI | 단일 서버 플러그인에 과잉 스펙, 명령어로 충분 |
| 2 | 칭호 거래·양도·경매 | 개인 달성 기반 체계와 충돌 |
| 3 | 실시간 리더보드/순위표 | MVP 범위 벗어남, 통계 DB 부담 |
| 4 | 한국어 외 다국어(i18n) | 단일 한국어 서버 컨텍스트 |
| 5 | 크로스 서버 동기화 | 단일 서버 전용 설계 |
| 6 | 외부 플러그인 연동 (LuckPerms/Vault/PlaceholderAPI) | 독립 구동 원칙 |
| 7 | 플레이어 커스텀 칭호 생성 | 운영자 정의 체계 유지 |
| 8 | 중복 달성 추가 보상 | 달성 1회성 원칙 |
| 9 | 통계 REST/GraphQL API | 플러그인 책임 영역 밖 |
| 10 | DB 자동 백업/복구 | 서버 인프라 영역 |
| 11 | GUI(인벤토리) 칭호 화면 | 명령어 기반 인터페이스 충분 |

---

## 3. 기술 명세 (Technical Specifications)

### 3.1 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────┐
│                       Paper Server                          │
│                                                             │
│  ┌──────────────┐   ┌──────────────────┐   ┌────────────┐  │
│  │ Bukkit Events│──>│  Stat Tracker    │──>│ Achievement│  │
│  │ (11종 리스너) │   │  (event→stat증가)│   │  Checker   │  │
│  └──────────────┘   └────────┬─────────┘   └─────┬──────┘  │
│                              │                   │         │
│                              ▼                   ▼         │
│                     ┌────────────────┐  ┌────────────────┐ │
│                     │ MySQL (async,  │  │ Hardcoded       │ │
│                     │ virtual thread)│  │ RewardProvider  │ │
│                     │ player_stats   │  │ (item/effect/   │ │
│                     └────────┬───────┘  │  command)       │ │
│                              │          └────────────────┘ │
│                              │                             │
│  ┌──────────────┐   ┌────────▼─────────┐                   │
│  │ Chat/Tab/    │<──│ Prefix Display & │                   │
│  │ Nametag 채널 │   │ Equip Manager    │                   │
│  └──────────────┘   └──────────────────┘                   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Command Layer (/칭호 정보|목록|장착|해제 + 관리자용)  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**데이터 플로우**: Bukkit 이벤트 → 통계 증가(MySQL) → 달성 검사 → 하드코딩된 보상 지급(RewardProvider) → (플레이어 선택 시) 칭호 장착 → 3채널 표시.

### 3.2 현재 구현 상태 (코드베이스 매핑)

| 컴포넌트 | 클래스/파일 | 상태 |
|----------|-------------|------|
| 플러그인 진입점 | `MCPrefixAchievement.java` | ✅ 구현 (DB/Config/Command 초기화) |
| 칭호 명령어(정보/목록) | `command/PrefixCommand.java` | ✅ 구현 |
| 칭호 모델 | `model/Prefix.java` | ✅ 구현 (display/stat/value/desc/reward(display-only)) |
| 통계 열거형 | `model/PrefixStat.java` | ✅ 구현 (11종) |
| Config 로더 | `config/PrefixConfigLoader.java` | ✅ 구현 |
| Config 매니저 | `config/PrefixConfigManager.java` | ✅ 구현 (reloadConfig 메서드 존재) |
| MySQL 래퍼 | `database/MysqlDatabase.java` | ✅ 구현 (virtual thread, runAsync/supplyAsync) |
| DB Config | `database/MysqlDatabaseConfig.java` | ✅ 구현 |
| 통계 추적 리스너 | — | ❌ 미구현 |
| 달성 검사/보상 지급 | `RewardProvider` (하드코딩) | ❌ 미구현 |
| DB 스키마(DAO/Repository) | — | ❌ 미구현 |
| 칭호 표시/장착 | — | ❌ 미구현 |
| 관리자 명령 | — | ❌ 미구현 (reloadConfig 메서드만 존재) |

### 3.3 DB 스키마 설계

`MysqlDatabase`가 제공하는 `runAsync`/`supplyAsync`를 통해 아래 3개 테이블을 운영한다. 모든 쓰기는 UPSERT(`ON DUPLICATE KEY UPDATE`)로 정합성을 보장한다.

```sql
-- (1) 플레이어 통계 (increment 대상)
CREATE TABLE player_stats (
    uuid        VARCHAR(36)  NOT NULL,
    stat        VARCHAR(32)  NOT NULL,        -- PrefixStat enum name
    value       BIGINT       NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, stat),
    INDEX idx_stat (stat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (2) 플레이어 보유 칭호 (달성 이력, 중복 방지)
CREATE TABLE player_prefixes (
    uuid         VARCHAR(36)  NOT NULL,
    prefix_id    INT          NOT NULL,       -- prefix.yml의 키
    achieved_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid, prefix_id),
    INDEX idx_prefix (prefix_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (3) 플레이어 장착 칭호 (1:1, 한 번에 1개)
CREATE TABLE player_equipped (
    uuid        VARCHAR(36)  NOT NULL,
    prefix_id   INT          NULL,            -- NULL = 미장착
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**쓰기 패턴**
- 통계 증가: `INSERT INTO player_stats VALUES(?, ?, ?+1) ON DUPLICATE KEY UPDATE value=value+1`
- 달성 기록: `INSERT IGNORE INTO player_prefixes VALUES(?, ?)` (중복 방지)
- 장착 변경: `INSERT INTO player_equipped VALUES(?, ?) ON DUPLICATE KEY UPDATE prefix_id=?`

### 3.4 통계 추적 이벤트 매핑

`PrefixStat` 열거형(`PrefixStat.java:3-15`) 11종 각각에 대응하는 Bukkit/Paper 이벤트를 리스너로 등록한다.

| PrefixStat | 대응 이벤트 | 추출 로직 |
|------------|-------------|-----------|
| `ANY_DEATH_COUNT` | `PlayerDeathEvent` | 카운트 +1 |
| `LAVA_DEATH_COUNT` | `PlayerDeathEvent` | `entity.getLastDamageCause().getCause() == LAVA` 필터 |
| `VOID_DEATH_COUNT` | `PlayerDeathEvent` | damage cause == `VOID` 필터 |
| `BREAK_BLOCK` | `BlockBreakEvent` | 카운트 +1 (모든 블럭) |
| `BREAK_DIAMOND_ORE` | `BlockBreakEvent` | `block.getType()` 다이아몬드 원석 계열 필터 |
| `PLACE_BLOCK` | `BlockPlaceEvent` | 카운트 +1 |
| `FIRST_JOIN` | `PlayerJoinEvent` | 최초 1회(`firstJoin=true` 플래그 또는 `player_stats` 미존재 시) |
| `KILL_ENDER_DRAGON` | `EntityDeathEvent` | `entity.getType() == ENDER_DRAGON` and killer is Player |
| `KILL_ENDERMAN_BY_MACE` | `EntityDeathEvent` | `entity.getType() == ENDERMAN` and killer's item type == `MACE` |
| `GET_DRAGON_BREATH` | `PlayerItemPickup`/`EntityPickupItemEvent` | item type == `DRAGON_BREATH` |
| `GET_DRAGON_EGG` | 인벤토리 이동/파괴 이벤트 | 드래곤 알 획득 감지 (블럭 파괴/아이템 획득 조합) |

**비동기 처리**: 이벤트 리스너는 메인 스레드에서 통계 증가를 큐에 넣고, `MysqlDatabase.runAsync`를 통해 virtual thread에서 DB 갱신. 고빈도 이벤트(`BREAK_BLOCK`/`PLACE_BLOCK`)는 인메모리 버퍼링 후 배치 플러시(예: 30초/10카운트 임계값)로 DB 부하 완화.

### 3.5 칭호 달성 검사 + 보상 자동 지급

#### 3.5.1 달성 검사 플로우

```
통계 갱신(3-4)
   │
   ▼
해당 stat을 requiredStat으로 갖는 모든 Prefix 조회
   │  (PrefixConfigManager.getPrefixMap() 필터)
   ▼
value >= requiredStatValue ?
   │
   ├─ No → 종료
   └─ Yes → player_prefixes에 이미 존재?
              ├─ Yes → 종료 (중복 방지)
              └─ No  → [달성 처리] ↓
                        1. INSERT IGNORE player_prefixes
                        2. 보상 지급 (3-5.2)
                        3. 알림 메시지(MiniMessage)
```

#### 3.5.2 보상 자동 지급 (하드코딩)

보상 지급은 `prefix.yml`의 reward 필드(표시 전용)와 분리되어, Java 코드에 하드코딩된 `RewardProvider`가 처리한다. `prefix.yml`의 reward 필드는 플레이어에게 보여주기 위한 자유 텍스트 문자열로 유지되며, 실제 보상 지급 로직은 코드에 직접 기재한다.

**RewardProvider 클래스 설계**

```java
public class RewardProvider {
    private final Map<Integer, RewardAction> rewards = new HashMap<>();

    // 초기화 시 prefix ID별 보상 액션 하드코딩 등록
    public RewardProvider() {
        register(1, RewardAction.builder()
            .items(ItemStack.of(Material.BREAD, 16), ItemStack.of(Material.TORCH, 32))
            .message("<gold>첫걸음 칭호를 획득했습니다!")
            .build());
        register(2, RewardAction.builder()
            .effects(new PotionEffect(PotionEffectType.HASTE, 6000, 1))
            .message("<gold>보석 수집가 칭호를 획득했습니다!")
            .build());
        // ... 나머지 칭호
    }

    public void dispatch(@NotNull Player player, int prefixId) {
        RewardAction action = rewards.get(prefixId);
        if (action == null) return; // 보상 없음
        action.execute(player);
    }
}
```

**RewardAction 구조**
- `items`: `List<ItemStack>` — `Player.getInventory().addItem(...)` (가득 찬 경우 월드에 드롭)
- `effects`: `List<PotionEffect>` — `Player.addPotionEffect(...)`
- `commands`: `List<String>` — `Bukkit.dispatchCommand(consoleSender, cmd)` (`{player}` 플레이스홀더 치환)
- `message`: `Component` — MiniMessage 직렬화 후 플레이어에게 전송

**지급 로직** (메인 스레드 동기 실행)
- `items`: `Player.getInventory().addItem(...)` (가득 찬 경우 드롭)
- `effects`: `Player.addPotionEffect(...)`
- `commands`: `Bukkit.dispatchCommand(consoleSender, cmd)` (`{player}` 플레이스홀더 치환)
- `message`: MiniMessage 직렬화 후 전송
- 달성 검사/DB 기록은 비동기, 보상 지급만 동기

**하드코딩 매핑 (12종 칭호)**

| prefix ID | reward 표시 텍스트 (prefix.yml) | 하드코딩된 보상 (RewardProvider) |
|-----------|--------------------------------|--------------------------------|
| 1 | "빵 16개, 횃불 32개" | `items: [BREAD×16, TORCH×32]` |
| 2 | "성급함 효과 5분" | `effects: [HASTE, 6000t, amp 1]` |
| 3 | "없음" | 빈 RewardAction (보상 없음) |
| 4 | "화염 저항 포션 1개(지속 시간 3분)" | `items: [FIRE_RESISTANCE_POTION×1]` |
| 5 | "본인의 머리(플레이어 머리)" | `items: [PLAYER_HEAD×1]` (머리 주인=플레이어) |
| 6 | "없음" | 빈 RewardAction (보상 없음) |
| 7 | "점프 강화 명령어(쿨타임 1시간, 유지시간 3분)" | `commands: ["jumpboost {player} 3"]` |
| 8 | "비계 64개" | `items: [SCAFFOLDING×64]` |
| 9 | "다이아몬드 삽(내구성 3)" | `items: [DIAMOND_SHOVEL, Damage=3]` |
| 10 | "스펀지 5개" | `items: [SPONGE×5]` |
| 11 | "용의 콧물" | `items: [DRAGON_BREATH×1]` |
| 12 | "마법이 부여된 횡금사과 1개" | `items: [ENCHANTED_GOLDEN_APPLE×1]` |

**주의사항**
- `prefix.yml`의 reward 표시 텍스트와 `RewardProvider` 하드코딩 내용이 일치해야 한다.
- 신규 칭호 추가 시 `prefix.yml`과 `RewardProvider.java` **두 곳을 동시 수정**해야 한다.
- 보상 내용 변경 시 재컴파일 및 재배포가 필요하다 (YAML 핫리로드 불가).

### 3.6 칭호 표시 및 장착

#### 3.6.1 장착 메커니즘 (1개 장착)

- `/칭호 장착 <ID>` — 미획득 시 거부, 보유 시 `player_equipped.prefix_id` 갱신(기존값 덮어쓰기 = 자동 해제).
- `/칭호 해제` — `player_equipped.prefix_id = NULL`.
- 장착 변경 즉시 3채널 표시 갱신.

#### 3.6.2 표시 채널

| 채널 | 구현 방식 | on/off |
|------|-----------|--------|
| 채팅 | `AsyncChatEvent` 리스너, `originalMessage` 앞에 `displayPrefix` + 공백 prepend | `config.yml: display.chat` |
| 탭리스트 | `Player.setPlayerListName()` 또는 `PlayerListHeaderFooter` | `config.yml: display.tablist` |
| 네임택 | `player.customName()` + `customNameVisible(true)`, 또는 `Team` prefix 속성 | `config.yml: display.nametag` |

**동기화**: 장착 변경 시 3채널 모두 동일 칭호로 갱신. `display.*`가 false인 채널은 건드리지 않음.

#### 3.6.3 캐시

- 접속 시 `player_equipped`/`player_prefixes`를 메모리 캐시(`Map<UUID, ...>`)로 로드.
- 표시는 캐시 기반 동기 처리, DB 동기화는 비동기.
- 퇴장 시 캐시 제거.

### 3.7 명령어 스펙

#### 3.7.1 플레이어용

| 명령어 | 설명 | 권한 | 상태 |
|--------|------|------|------|
| `/칭호` | 사용 가능 명령어 목록 | 전체 | ✅ |
| `/칭호 정보 <ID>` | 특정 칭호 정보 | 전체 | ✅ |
| `/칭호 목록 [페이지]` | 칭호 목록(15개/페이지) | 전체 | ✅ |
| `/칭호 보유` | 내 보유 칭호 목록 | 전체 | ❌ 신규 |
| `/칭호 장착 <ID>` | 칭호 장착(1개) | 전체 | ❌ 신규 |
| `/칭호 해제` | 칭호 해제 | 전체 | ❌ 신규 |

#### 3.7.2 관리자용

| 명령어 | 설명 | 권한 |
|--------|------|------|
| `/칭호 리로드` | config/prefix.yml 재적재 | `mcprefixachievement.admin.reload` |
| `/칭호 지급 <플레이어> <ID>` | 칭호 수동 지급 | `mcprefixachievement.admin.grant` |
| `/칭호 회수 <플레이어> <ID>` | 칭호 회수 | `mcprefixachievement.admin.revoke` |
| `/칭호 초기화 <플레이어> [통계]` | 통계/칭호 초기화 | `mcprefixachievement.admin.reset` |
| `/칭호 조회 <플레이어>` | 보유/통계/장착 조회 | `mcprefixachievement.admin.lookup` |

탭 완성(TabCompleter) 지원 필수.

### 3.8 Config 스키마

#### 3.8.1 config.yml (현재 + 확장)

```yaml
database:
    address: localhost:3306
    username: root
    password: ""
    database: minecraft
    properties: {}

# [신규] 표시 채널 on/off
display:
    chat: true
    tablist: true
    nametag: true

# [신규] 고빈도 통계 버퍼링
stats:
    batch-flush-interval-seconds: 30
    batch-flush-threshold: 10

# [신규] 달성 알림
notification:
    on-achieve: true
    on-equip: true
```

#### 3.8.2 prefix.yml reward 필드 (표시 전용)

`reward` 필드는 플레이어에게 보여주기 위한 **표시 전용 텍스트**로 유지된다. 실제 보상 지급은 `RewardProvider`(Java 하드코딩, 섹션 3.5.2 참조)에서 처리하므로, 구조화된 스키마 마이그레이션은 불필요하다. 기존 12종 칭호의 reward 자유 텍스트는 그대로 유지한다.

**운영자가 신규 칭호를 추가할 때:**
1. `prefix.yml`에 칭호 정의 (reward 필드에 표시용 텍스트 기재)
2. `RewardProvider.java`에 해당 ID의 보상 액션 하드코딩
3. 재컴파일 후 배포

**트레이드오프:**
- YAML 구조화 파싱 리스크 제거 (잘못된 material/effect로 인한 지급 실패 방지)
- 보상 내용 변경 시 재컴파일 필요 (YAML 핫리로드 불가)
- `prefix.yml` reward 표시 텍스트 ↔ `RewardProvider` 하드코딩 보상 불일치 리스크 (코드 리뷰로 관리)

---

## 4. 리스크 및 로드맵 (Risks & Roadmap)

### 4.1 단계별 롤아웃

| 단계 | 범위 | 산출물 |
|------|------|--------|
| **MVP** | DB 스키마 + 통계 추적 리스너(11종) + 달성 검사 + 보상 자동 지급 | 코어 루프(행동→통계→달성→보상) 완성 |
| **v1.1** | 칭호 장착 + 3채널 표시(채팅/탭/네임택) + 메모리 캐시 | 표시 체계 완성 |
| **v1.2** | 관리자 명령(리로드/지급/회수/초기화/조회) + TabCompleter | 운영 도구 완성 |
| **v1.3** | 하드코딩 보상 로직 안정화(RewardProvider) + 버퍼링 튜닝 | 운영 안정화 |

### 4.2 기술적 리스크

| # | 리스크 | 영향 | 완화 방안 |
|---|--------|------|-----------|
| 1 | 고빈도 이벤트(BreakBlock/PlaceBlock) DB 부하 | 서버 TPS 저하 | 인메모리 버퍼링 + 배치 플러시(3.8.1 `stats.batch-*`) |
| 2 | 비동기 DB I/O 중 플레이어 퇴장 | 캐시/DB 불일치 | `runAsync` CompletableFuture 조인 또는 퇴장 시 flush 대기 |
| 3 | 동시 통계 갱신 경쟁(race condition) | 통계 누락/중복 | UPSERT(`ON DUPLICATE KEY UPDATE value=value+1`)로 원자성 보장 |
| 4 | 하드코딩 보상 로직 예외(item/effect null) | 보상 미지급 | `RewardProvider` 단위 테스트, `dispatch` 시 try-catch + 로그 |
| 5 | `prefix.yml` reward 표시 텍스트 ↔ `RewardProvider` 하드코딩 보상 불일치 | 플레이어 혼란 | 코드 리뷰 체크리스트에 두 곳 동시 수정 항목 포함 |
| 6 | 네임택/탭리스트 표시가 타 플러그인과 충돌 | 표시 깨짐 | `display.*` 채널별 on/off, 이벤트 우선순위 조정 |
| 7 | `GET_DRAGON_EGG` 감지 신뢰성 | 달성 누락 | 다중 이벤트(블럭 파괴/아이템 획득) 조합 감지, 보수적 집계 |
| 8 | MySQL 연결 실패 시 플러그인 비활성화 | 전체 기능 정지 | `MCPrefixAchievement.onEnable`의 try-catch 유지, 재시도 로직 검토 |

---

> **참고**: 본 PRD는 `prd` 스키마(Executive Summary / UX&Functionality / Technical Specs / Risks&Roadmap)를 준수한다. AI 시스템 요구사항(섹션 3)은 해당 없음(비-AI 프로젝트).
