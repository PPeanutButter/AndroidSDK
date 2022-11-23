# CacheStoreCookieJar
> 以自动化的方式管理OkhttpClient的Cookie

🎉Features
- 内存缓存(HashMap)
- 磁盘存储(Jetpack Room Database)

`implementation 'com.github.PPeanutButter:AndroidSDK:Tag'`

示例
```kotlin
val client = OkHttpClient.Builder().cookieJar(CacheStoreCookieJar(context)).build()
```
