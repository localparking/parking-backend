-----

# 🅿️ TownParking (Backend)

<img width="1621" height="912" alt="Image" src="https://github.com/user-attachments/assets/93162824-0f96-4c4c-9d59-356f7092a798" />
<br>
<br>

**"주차비 낼 돈으로, 커피 한 잔 더."** <br>
서울 도심의 주차난과 비용 문제를 '상점 이용 = 주차 혜택'으로 해결하는 <br>
**로컬 상생 플랫폼**입니다.


<br>

## 💡 Background

서울시내 주차 공간 확보율은 147.8%에 달하지만, 여전히 불법 주정차 민원이 전체의 55%를 차지합니다. <br>
우리는 주차 공간 부족이 아니라, '비싼 요금과 심리적 거부감'이 진짜 문제라고 판단했습니다.

  * **Problem:** 주차장은 많지만 비싸서 이용하지 않음 -\> 불법 주정차 만연
  * **Solution:** 소멸되는 비용인 '주차비'를 상점에서의 '소비(혜택)'로 전환
  * **Goal:** 운전자는 주차비 절감, 상인은 매출 증대, 도시는 불법 주정차 해소

<br>

## 🛠 Tech Stack

| Category | Stack & Version |
| :--- | :--- |
| **Language** | **Kotlin** (JDK 17) |
| **Framework** | **Spring Boot** |
| **Database** | **MySQL** 8.0 <br> **Redis** |
| **Search** | **Elasticsearch** (Geo-spatial, Nori Tokenizer) |
| **Infra** | **AWS** (EC2, RDS, S3), **Docker** |
| **Auth** | **Spring Security**, OAuth2 (Kakao/Apple), JWT |
| **Open API** | Seoul Parking API, Naver Search, Toss Payments |

<br>

## 🔑 Key Features

### 1\. 지도 & 검색 (Search & Geo)

  * **고성능 검색 엔진:** Elasticsearch와 Nori 형태소 분석기를 도입해, 단순 일치 검색보다 정확한 상점/주차장 키워드 검색을 구현했습니다.
  * **위치 기반 탐색:** GeoPoint 쿼리를 활용하여 사용자 위치 기준 반경 2km/4km 내의 주차장과 제휴 상점을 빠르게 필터링합니다.
  * **상세 필터:** 거리순/가격순 정렬은 물론, 운영 시간이나 무료 주차 가능 여부 등 다양한 조건으로 검색할 수 있습니다.

### 2\. 실시간 주차 현황 (Real-time Data)

  * **데이터 동기화:** `ParkingRealtimeDataSyncService`를 통해 서울시 공공 데이터를 주기적으로 수집하여 최신 주차 정보를 유지합니다.
  * **Redis 캐싱:** 조회 빈도가 높은 실시간 혼잡도(여유/보통/혼잡) 정보는 Redis에 캐싱하여 DB 부하를 줄이고 응답 속도를 높였습니다.

### 3\. 주문 및 자동 정산 (Commerce)

  * **주차 혜택 자동화:** `FeeCalculationUtil`을 통해 상품 결제 금액에 따른 무료 주차 시간과 잔여 요금을 자동으로 계산합니다.
  * **안전한 결제:** 주문 검증 로직(`OrderService`)과 Toss Payments 연동을 통해 결제 무결성을 보장합니다.

### 4\. 점주/관리자 기능 (Management)

  * **매장 관리:** 영업시간 설정 및 위치 보정, AWS S3를 이용한 상품 이미지 등록 기능을 제공합니다.
  * **제휴 시스템:** 점주가 직접 인근 주차장을 검색하여 내 가게와 제휴(Link/Unlink)를 맺을 수 있도록 구현했습니다.

<br>

## 📂 Project Structure

핵심 도메인 위주로 구성된 패키지 구조입니다.

```bash
com.spring.localparking
├── api           # 서울시 공공 데이터 연동 (Client, Sync)
├── auth          # 인증/인가 (OAuth2, JWT, Security)
├── search        # 검색 엔진 연동 (ES Document, Service)
├── store         # 가게 및 상품 도메인
├── parking       # 주차장 및 요금 정책(FeePolicy)
├── order         # 주문 처리 및 결제 검증
├── storekeeper   # 점주 전용 기능 (혜택/상품 관리)
└── global        # 전역 설정 (Config, Exception, Utils)
```
