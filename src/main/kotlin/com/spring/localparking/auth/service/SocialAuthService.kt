package com.spring.localparking.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import com.spring.localparking.auth.dto.AppleSocialTokenInfoResponseDto
import com.spring.localparking.auth.dto.KakaoUserMe
import com.spring.localparking.global.Provider
import com.spring.localparking.user.domain.User
import com.spring.localparking.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.security.PrivateKey
import java.security.Security
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Transactional
class SocialAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    @Value("\${apple.client-id}") private val appleClientId: String,
    @Value("\${apple.key-id}") private val appleKeyId: String,
    @Value("\${apple.team-id}") private val appleTeamId: String,
    @Value("\${apple.audience}") private val appleAudience: String,
    @Value("\${apple.grant-type}") private val appleGrantType: String,
    @Value("\${apple.private-key}") private val applePrivateKey: String
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

    fun loginApple(authorizationCode: String): User {
        val response = getAppleTokenInfo(authorizationCode)
        val idToken = response.idToken

        val decodedJWT: DecodedJWT = JWT.decode(idToken)

        require(decodedJWT.getClaim("iss").asString() == appleAudience)
        require(decodedJWT.getClaim("aud").asString() == appleClientId)

        val appleSub = decodedJWT.getClaim("sub").asString()
        val email = decodedJWT.getClaim("email").asString()
        val nickname = email ?: appleSub

        return userRepository.findByProviderAndProviderId(Provider.APPLE, appleSub)
            ?: userRepository.save(User.ofProvider(Provider.APPLE, appleSub, nickname, email))
    }

    private fun getAppleTokenInfo(code: String): AppleSocialTokenInfoResponseDto {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val params = LinkedMultiValueMap<String, String>()
        params.add("client_id", appleClientId)
        params.add("client_secret", generateClientSecret())
        params.add("grant_type", appleGrantType)
        params.add("code", code)

        val request = HttpEntity(params, headers)

        val response = restTemplate.exchange(
            "$appleAudience/auth/token",
            HttpMethod.POST,
            request,
            AppleSocialTokenInfoResponseDto::class.java
        )

        return response.body ?: throw IllegalStateException("Apple token response is null")
    }

    private fun generateClientSecret(): String {
        val expiration = LocalDateTime.now().plusMinutes(5)
        return Jwts.builder()
            .setHeaderParam(JwsHeader.KEY_ID, appleKeyId)
            .setIssuer(appleTeamId)
            .setAudience(appleAudience)
            .setSubject(appleClientId)
            .setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
            .setIssuedAt(Date())
            .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
            .compact()
    }

    private fun getPrivateKey(): PrivateKey {
        Security.addProvider(BouncyCastleProvider())
        val converter = JcaPEMKeyConverter().setProvider("BC")
        try {
            val privateKeyBytes = Base64.getDecoder().decode(applePrivateKey)
            val privateKeyInfo = PrivateKeyInfo.getInstance(privateKeyBytes)
            return converter.getPrivateKey(privateKeyInfo)
        } catch (e: Exception) {
            throw RuntimeException("Error converting private key from String", e)
        }
    }

}