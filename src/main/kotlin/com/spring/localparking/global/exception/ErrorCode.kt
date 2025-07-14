package com.spring.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // COMMON
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),

    // AUTH
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 없습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 사용자입니다."),

    //TERM
    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 약관입니다."),
    REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "필수 약관에 동의하지 않았습니다."),
    MISSING_REQUIRED_TERMS(HttpStatus.BAD_REQUEST, "모든 약관에 응답해주세요."),

    //ONBOARDING
    ALREADY_ONBOARDED(HttpStatus.BAD_REQUEST, "이미 온보딩이 완료된 사용자입니다."),
    INVALID_AGE_GROUP(HttpStatus.BAD_REQUEST, "유효하지 않은 연령대입니다."),
    INVALID_WEIGHT(HttpStatus.BAD_REQUEST, "유효하지 않은 가중치 값입니다."),

    //CATEGORY
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),

    //PARKING
    PARKING_LOT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주차장입니다."),
    FEE_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 요금 정책입니다."),
    OPERATING_HOUR_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 운영 시간입니다."),

    //TYPE
    SORT_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 타입입니다."),
}
