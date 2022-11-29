package com.peanut.nas.sdk

import android.app.Activity
import android.os.Bundle
import com.peanut.sdk.petlin.FileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MainScope().launch {
            FileCompat.saveFileToPublicDownload(this@MainActivity, dir = "password manager", fileName = "test.bin"){
                withContext(Dispatchers.IO) {
                    it.write("Hello World!".toByteArray())
                }
            }
        }

    }
}