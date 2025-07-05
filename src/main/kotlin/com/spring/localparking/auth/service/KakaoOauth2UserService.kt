package com.spring.localparking.auth.service

import com.spring.localparking.auth.security.CustomPrincipal
import com.spring.localparking.global.Provider
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class KakaoOauth2UserService(
    private val userRepository: UserRepository

) : DefaultOAuth2UserService() {

    override fun loadUser(req: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(req)
        val attr = oAuth2User.attributes

        val kakaoId = (attr["id"] as? Number)?.toLong() ?: throw OAuth2AuthenticationException("Missing Kakao ID")

        val props = attr["properties"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val nickname = props["nickname"]?.toString() ?: "unknown"
        val kakaoAccount = attr["kakao_account"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val email = kakaoAccount["email"]?.toString() ?: "unknown"

        val user = userRepository.findByProviderId(kakaoId)
            ?: userRepository.save(User.ofProvider(Provider.KAKAO, kakaoId, nickname, email))

        return CustomPrincipal(user, attr)
    }
}
