package com.peanut.sdk.okhttp3

import okhttp3.Cookie

interface OnReceiveCookieCallback {
    fun onReceive(cookie: Cookie)
}