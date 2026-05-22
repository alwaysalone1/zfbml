package com.zfbml.aggregate.auth

import android.webkit.CookieManager

data class AuthSession(
    val domain: String,
    val cookieHeader: String?,
    val isAuthenticated: Boolean,
)

interface AuthSessionStore {
    fun session(domain: String): AuthSession

    fun cookieHeader(domain: String): String?

    fun clear(domain: String)
}

class WebViewCookieAuthSessionStore(
    private val cookieManager: CookieManager = CookieManager.getInstance(),
) : AuthSessionStore {
    override fun session(domain: String): AuthSession {
        val cookie = cookieHeader(domain)
        return AuthSession(
            domain = domain,
            cookieHeader = cookie,
            isAuthenticated = cookie.isNullOrBlank().not(),
        )
    }

    override fun cookieHeader(domain: String): String? {
        val url = if (domain.startsWith("http://") || domain.startsWith("https://")) {
            domain
        } else {
            "https://$domain"
        }
        return cookieManager.getCookie(url)
    }

    override fun clear(domain: String) {
        cookieManager.setCookie("https://$domain", "")
        cookieManager.flush()
    }
}
