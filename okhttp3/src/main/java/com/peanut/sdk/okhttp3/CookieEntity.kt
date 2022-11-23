package com.peanut.sdk.okhttp3

import androidx.room.Entity
import okhttp3.Cookie
import okhttp3.HttpUrl

@Entity(primaryKeys = ["name", "domain"])
data class CookieEntity(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
    val persistent: Boolean,
    val hostKey: String
){

    companion object{
        fun HttpUrl.key(): String = this.host
        fun Cookie.key() = this.name + this.domain
        fun fromCookie(cookie: Cookie, httpUrl: HttpUrl? = null):CookieEntity{
            return CookieEntity(
                name = cookie.name,
                value = cookie.value,
                expiresAt = cookie.expiresAt,
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                hostOnly = cookie.hostOnly,
                persistent = cookie.persistent,
                hostKey = httpUrl?.key()?:""
            )
        }
    }

    fun toCookie(): Cookie{
        return Cookie.Builder()
            .name(name)
            .value(value)
            .expiresAt(expiresAt)
            .path(path).apply {
                if (secure) this.secure()
                if (httpOnly) this.httpOnly()
                if (hostOnly) this.hostOnlyDomain(domain)
                else this.domain(domain)
            }
            .build()
    }


}