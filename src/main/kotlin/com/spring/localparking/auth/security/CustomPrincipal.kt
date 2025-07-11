package com.spring.localparking.auth.security

import com.spring.localparking.user.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

class CustomPrincipal (
    private val user: User,
    private val attrs: MutableMap<String, Any> = mutableMapOf()
    ) : OAuth2User, UserDetails{
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(user.role.value))

    override fun getPassword(): String = user.password ?:""

    override fun getUsername(): String = user.id.toString()

    override fun getAttributes(): MutableMap<String, Any> = attrs

    override fun getName(): String = user.nickname

    val id: Long? get() = user.id

    val role: String get() = user.role.value

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

}