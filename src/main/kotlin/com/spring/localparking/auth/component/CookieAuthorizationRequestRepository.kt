package com.spring.localparking.auth.component

import com.nimbusds.oauth2.sdk.util.StringUtils
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import java.io.*
import java.util.*

@Component
class CookieAuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
    }

    private val cookieExpireSeconds = 180

    private fun getCookieDomain(request: HttpServletRequest): String? {
        val rawHost = request.getHeader("X-Forwarded-Host")?.split(",")?.first()?.trim()
            ?: request.serverName
        val host = rawHost.substringBefore(":")
        val isIp = Regex("""^\d{1,3}(\.\d{1,3}){3}$""").matches(host)

        if (host.equals("localhost", true) || isIp) return null

        return when {
            host == "townparking.store" -> "townparking.store"
            host.endsWith(".townparking.store") -> "townparking.store"
            else -> null // 그 외는 host-only 권장
        }
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            ?.let { deserialize(it.value, OAuth2AuthorizationRequest::class.java) }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response)
            return
        }

        addCookie(
            response,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serialize(authorizationRequest),
            cookieExpireSeconds
        )
        val redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            addCookie(
                response,
                REDIRECT_URI_PARAM_COOKIE_NAME,
                redirectUriAfterLogin,
                cookieExpireSeconds
            )
        }
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest, response: HttpServletResponse): OAuth2AuthorizationRequest? {
        return this.loadAuthorizationRequest(request)
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        val cookieDomain = getCookieDomain(request)
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, cookieDomain)
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME, cookieDomain)
    }

    private fun getCookie(request: HttpServletRequest, name: String) =
        request.cookies?.find { it.name == name }

    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value).apply {
            path = "/"
            isHttpOnly = true
            secure = true
            this.maxAge = maxAge
        }
        response.addCookie(cookie)
    }


    private fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String, domain: String?)  {
        val del = Cookie(name, "").apply {
            path = "/"
            isHttpOnly = true
            secure = true
            maxAge = 0
            if (!domain.isNullOrBlank()) this.domain = domain
        }
        response.addCookie(del)
    }

    private fun serialize(obj: Any): String {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(obj) }
        return Base64.getUrlEncoder().encodeToString(baos.toByteArray())
    }

    private fun <T> deserialize(str: String, cls: Class<T>): T? {
        return try {
            val data = Base64.getUrlDecoder().decode(str)
            val ois = ObjectInputStream(ByteArrayInputStream(data))
            cls.cast(ois.readObject())
        } catch (e: Exception) {
            null
        }
    }
}