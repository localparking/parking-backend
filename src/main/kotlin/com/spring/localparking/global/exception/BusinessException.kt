package com.spring.localparking.global.exception

import com.spring.global.exception.ErrorCode

class BusinessException(errorCode : ErrorCode) : CustomException(errorCode)