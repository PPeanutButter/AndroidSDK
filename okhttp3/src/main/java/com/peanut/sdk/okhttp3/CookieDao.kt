package com.peanut.sdk.okhttp3

import androidx.room.*

@Dao
interface CookieDao {
    @Query("SELECT * FROM CookieEntity")
    suspend fun getAll(): List<CookieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg cookieEntity: CookieEntity)

    @Delete
    fun delete(vararg cookieEntity: CookieEntity)
}