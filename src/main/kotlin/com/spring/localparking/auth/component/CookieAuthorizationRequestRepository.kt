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

        val cookieDomain = getCookieDomain(request)

        addCookie(
            response,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            serialize(authorizationRequest),
            cookieExpireSeconds,
            cookieDomain
        )
        val redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            addCookie(
                response,
                REDIRECT_URI_PARAM_COOKIE_NAME,
                redirectUriAfterLogin,
                cookieExpireSeconds,
                cookieDomain
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

    private fun getCookieDomain(request: HttpServletRequest): String? {
        val origin = request.getHeader("Origin") ?: request.serverName
        return if (origin.contains("localhost")) null else ".townparking.store"
    }

    private fun getCookie(request: HttpServletRequest, name: String) =
        request.cookies?.find { it.name == name }

    private fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int, domain: String?) {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = maxAge
        domain?.let { cookie.domain = it }
        response.addCookie(cookie)
    }

    private fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String, domain: String?) {
        request.cookies?.filter { it.name == name }?.forEach {
            it.value = ""
            it.path = "/"
            it.maxAge = 0
            domain?.let { cookieDomain -> it.domain = cookieDomain }
            response.addCookie(it)
        }
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