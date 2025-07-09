package com.spring.localparking.auth.service

import com.spring.localparking.auth.dto.KakaoUserMe
import com.spring.localparking.global.Provider
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@Transactional
class SocialAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    @Qualifier("appleJwtDecoder") private val jwtDecoder: JwtDecoder,
    @Value("\${apple.client-id}") private val appleClientId: String
){
    fun loginKakao(accessToken: String): User {
        val res = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            HttpEntity<Void>(HttpHeaders().apply {
                setBearerAuth(accessToken)
            }),
            KakaoUserMe::class.java
        ).body ?: throw BadCredentialsException("invalid kakao token")

        val kakaoId  = res.id.toString()
        val nickname = res.properties?.nickname ?: "unknown"
        val email    = res.kakaoAccount?.email

        return userRepository.findByProviderAndProviderId(Provider.KAKAO, kakaoId)
            ?: userRepository.save(
                User.ofProvider(Provider.KAKAO, kakaoId, nickname, email)
            )
    }

    fun loginApple(identityToken: String): User {
        val jwt = try {
            jwtDecoder.decode(identityToken)
        } catch (ex: JwtException) {
            throw BadCredentialsException("invalid apple token", ex)
        }
        require(jwt.claims["iss"] == "https://appleid.apple.com")
        require(jwt.claims["aud"] == appleClientId)

        val appleSub = jwt.claims["sub"] as String
        val email    = jwt.claims["email"] as String?
        val nickname  = email ?: jwt.claims["sub"] as String

        return userRepository.findByProviderAndProviderId(Provider.APPLE, appleSub)
            ?: userRepository.save(User.ofProvider(Provider.APPLE, appleSub, nickname, email))
    }

}