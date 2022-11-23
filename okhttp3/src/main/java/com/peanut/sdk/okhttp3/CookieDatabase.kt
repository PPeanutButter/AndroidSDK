package com.peanut.sdk.okhttp3

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CookieEntity::class], version = 1, exportSchema = false)
abstract class CookieDatabase : RoomDatabase() {
    abstract fun cookieDao(): CookieDao
}