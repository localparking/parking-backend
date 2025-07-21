package com.spring.localparking.auth.service.social

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.spring.localparking.auth.dto.social.AppleLoginRequest
import com.spring.localparking.auth.dto.social.KakaoUserMe
import com.spring.localparking.global.dto.Provider
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.util.*

@Service
@Transactional
class SocialAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
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

    private val jwkSource = RemoteJWKSet<SecurityContext>(
        URL("https://appleid.apple.com/auth/keys")
    )
    private val jwtProcessor = DefaultJWTProcessor<SecurityContext>().apply {
        jwsKeySelector = JWSVerificationKeySelector(
            JWSAlgorithm.RS256,
            jwkSource
        )
    }
    fun verifyIdentityToken(idToken: String, expectedClientId: String): DecodedJWT {
        val claims = jwtProcessor.process(idToken, null)
        require(claims.issuer == "https://appleid.apple.com")
        require(claims.audience.contains(expectedClientId))
        require(claims.expirationTime.after(Date()))
        return JWT.decode(idToken)
    }
    fun loginApple(req: AppleLoginRequest): User {
        val jwt = verifyIdentityToken(req.identityToken, appleClientId)
        val sub = jwt.subject
        val email = jwt.getClaim("email").asString()
        val nickname = req.fullName ?: email?.substringBefore('@') ?: sub
        return userRepository.findByProviderAndProviderId(Provider.APPLE, sub)
            ?: userRepository.save(User.ofProvider(Provider.APPLE, sub, nickname, email))
    }
    fun loginAsGuest(): User {
        val guestId = "guest_${UUID.randomUUID()}"
        val guestUser = User.ofProvider(
            provider = Provider.NONE,
            providerId = guestId,
            nickname = "Guest",
            email = "$guestId@guest.com"
        )
        return userRepository.save(guestUser)
    }

}