package com.peanut.sdk.okhttp3

import android.content.Context
import androidx.room.Room
import com.peanut.sdk.okhttp3.CookieEntity.Companion.key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread


/**
 * 1.使用内存缓存
 * 2.保存时使用使用Room异步刷回磁盘持久化
 */
class CacheStoreCookieJar(context: Context, val callback: OnReceiveCookieCallback? = null) : CookieJar {
    private var database: CookieDatabase
    private var cookies: HashMap<String, ConcurrentHashMap<String, Cookie>>

    init {
        database = Room.databaseBuilder(context, CookieDatabase::class.java, "cookies").build()
        cookies = HashMap()
        MainScope().launch {
            val results = withContext(Dispatchers.IO) {
                database.cookieDao().getAll()
            }
            results.forEach {
                val cookie = it.toCookie()
                handleCallback(cookie)
                if (cookies.containsKey(it.hostKey)) {
                    cookies[it.hostKey]?.set(cookie.key(), cookie)
                } else {
                    cookies[it.hostKey] = ConcurrentHashMap<String, Cookie>().apply { this[cookie.name + cookie.domain] = cookie }
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            if (isCookieExpired(cookie)) {
                continue
            }
            this.saveFromResponse(url, cookie)
        }
        thread {
            database.cookieDao().insertAll(*(cookies.map { CookieEntity.fromCookie(it, url) }.toTypedArray()))
        }
    }

    private fun handleCallback(cookie: Cookie){
        try {
            callback?.onReceive(cookie)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun saveFromResponse(url: HttpUrl, cookie: Cookie) {
        handleCallback(cookie)
        if (!cookie.persistent) {
            return
        }
        val cookieKey = cookie.key()
        val hostKey = url.key()
        if (!cookies.containsKey(hostKey)) {
            cookies[hostKey] = ConcurrentHashMap()
        }
        cookies[hostKey]!![cookieKey] = cookie
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        val result = mutableListOf<Cookie>()
        val hostKey = url.key()
        if (cookies.containsKey(hostKey)) {
            val cookies: Collection<Cookie> = cookies[hostKey]!!.values
            for (cookie in cookies) {
                if (isCookieExpired(cookie)) {
                    this.remove(hostKey, cookie)
                } else {
                    result.add(cookie)
                }
            }
        }
        return result
    }

    private fun remove(hostKey: String, cookie: Cookie) {
        val cookieKey: String = cookie.key()
        if (cookies.containsKey(hostKey) && cookies[hostKey]!!.containsKey(cookieKey)) {
            cookies[hostKey]!!.remove(cookieKey)
            thread {
                database.cookieDao().delete(CookieEntity.fromCookie(cookie))
            }
        }
    }

    private fun isCookieExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt < System.currentTimeMillis()
    }
}