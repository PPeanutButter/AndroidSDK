# CacheStoreCookieJar
> 以自动化的方式管理OkhttpClient的Cookie

🎉Features
- 内存缓存(HashMap)
- 磁盘存储(Jetpack Room Database)

`implementation 'com.github.PPeanutButter.AndroidSDK:okhttp3:Tag'`

## 基础配置

> 完全不管理任何相关的东西

```kotlin
val client = OkHttpClient
			.Builder()
			.cookieJar(CacheStoreCookieJar(context))
			.build()
```

## 带回调

> 在初次加载本地`cookie`或者服务器端返回`Set-Cookie`时回调。

```kotlin
CacheStoreCookieJar(context = context){
    println(it.toString())
}
```

## Debug

> 如果发现有不对的地方可以查看日志调试，然后反馈给我。

```kotlin
CacheStoreCookieJar(context = context, debug = true){
    println(it.toString())
}
```

