package com.spring.localparking.user.exception

import com.spring.global.exception.ErrorCode
import com.spring.localparking.global.exception.CustomException

class UserNotFoundException:CustomException(ErrorCode.USER_NOT_FOUND)