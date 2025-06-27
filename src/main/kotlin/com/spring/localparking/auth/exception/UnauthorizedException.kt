package com.spring.localparking.auth.exception

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.exception.CustomException

class UnauthorizedException : CustomException(ErrorCode.UNAUTHORIZED)