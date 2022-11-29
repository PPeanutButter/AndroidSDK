package com.peanut.sdk.okhttp3

import android.content.Context
import okhttp3.Cookie
import okhttp3.OkHttpClient
import okhttp3.Request


class CookieJarTest(context: Context) {
    private var okHttpClient: OkHttpClient? = null
    private val cookieJar = CacheStoreCookieJar(context = context, debug = true){
        println(it.toString())
    }

    private fun getHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            synchronized("okHttpClient") {
                if (okHttpClient == null) {
                    okHttpClient = OkHttpClient.Builder()
                        .cookieJar(cookieJar)
                        .build()
                }
            }
        }
        return okHttpClient!!
    }

    fun test(){
        val client = getHttpClient()
        var request: Request = Request.Builder()
            .url("https://bilibili.com/")
            .build()
        var r = client.newCall(request).execute()
        println(r.headers.toString())
        request = Request.Builder()
            .url("https://gitee.com/peanutbutter/dashboard/projects")
            .build()
        r = client.newCall(request).execute()
        println(r.headers.toString())
    }
}