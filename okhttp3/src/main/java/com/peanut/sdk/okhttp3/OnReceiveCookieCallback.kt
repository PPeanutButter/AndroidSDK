package com.peanut.sdk.okhttp3

import okhttp3.Cookie

@FunctionalInterface
interface OnReceiveCookieCallback {
    fun onReceive(cookie: Cookie)
}