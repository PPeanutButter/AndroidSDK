package com.peanut.nas.sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.peanut.sdk.petlin.Extend.describeAsFileSize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        12345678L.describeAsFileSize(" ")
    }
}