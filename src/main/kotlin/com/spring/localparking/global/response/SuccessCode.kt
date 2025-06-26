package com.spring.localparking.global.response

enum class SuccessCode (val message: String){
    OK("요청이 성공적으로 처리되었습니다."),
    USER_CREATED("회원가입이 완료되었습니다."),
    USER_LOGGED_IN("로그인 성공")

}