package com.peanut.sdk.okhttp3

import okhttp3.Cookie

typealias OnReceiveCookieCallback = (cookie: Cookie) -> Unit
//@FunctionalInterface
//interface OnReceiveCookieCallback {
//    fun onReceive(cookie: Cookie)
//}