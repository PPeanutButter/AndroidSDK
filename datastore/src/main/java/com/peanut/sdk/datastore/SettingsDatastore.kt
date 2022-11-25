package com.peanut.sdk.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


/**
 * 给Settings Activity使用的替代SharedPreference的方案
 * @param scope 默认为 GlobalScope, 你也可以指定为SettingActivity的lifecycleScope或者viewModelScope
 * @param dataStore 一个单例Singleton DataStore对象用来存取设置数据，不内置方便你在别的地方读取
 * 确保在 setPreferencesFromResource(R.xml.root_preferences, rootKey) 之前调用
 */
@OptIn(DelicateCoroutinesApi::class)
class SettingsDatastore(private val scope: CoroutineScope = GlobalScope, private val dataStore: DataStore<Preferences>, private val debug: Boolean = false): PreferenceDataStore() {

    companion object{
        private const val TAG = "SettingsDatastore"
    }

    override fun putInt(key: String?, value: Int) {
        if (debug) Log.d(TAG, "put: $key=$value")
        scope.launch {
            putIntImpl(key, value)
        }
    }

    override fun getInt(key: String?, defValue: Int): Int {
        var getValue: Int
        runBlocking {
            getValue = getIntImpl(key, defValue)
        }
        if (debug) Log.d(TAG, "get: $key=$getValue")
        return getValue
    }

    override fun putLong(key: String?, value: Long) {
        if (debug) Log.d(TAG, "put: $key=$value")
        scope.launch {
            putLongImpl(key, value)
        }
    }

    override fun getLong(key: String?, defValue: Long): Long {
        var getValue: Long
        runBlocking {
            getValue = getLongImpl(key, defValue)
        }
        if (debug) Log.d(TAG, "get: $key=$getValue")
        return getValue
    }

    override fun putFloat(key: String?, value: Float) {
        if (debug) Log.d(TAG, "put: $key=$value")
        scope.launch {
            putFloatImpl(key, value)
        }
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        var getValue: Float
        runBlocking {
            getValue = getFloatImpl(key, defValue)
        }
        if (debug) Log.d(TAG, "get: $key=$getValue")
        return getValue
    }

    override fun putBoolean(key: String?, value: Boolean) {
        if (debug) Log.d(TAG, "put: $key=$value")
        scope.launch {
            putBooleanImpl(key, value)
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        var getValue: Boolean
        runBlocking {
            getValue = getBooleanImpl(key, defValue)
        }
        if (debug) Log.d(TAG, "get: $key=$getValue")
        return getValue
    }

    override fun putString(key: String?, value: String?) {
        if (debug) Log.d(TAG, "put: $key=$value")
        scope.launch {
            putStringImpl(key, value)
        }
    }

    override fun getString(key: String?, defValue: String?): String? {
        var getValue: String?
        runBlocking {
            getValue = getStringImpl(key, defValue)
        }
        if (debug) Log.d(TAG, "get: $key=$getValue")
        return getValue
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        if (debug) Log.d(TAG, "put: $key=$values")
        scope.launch {
            putStringSetImpl(key, values)
        }
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String> {
        val getValue = mutableSetOf<String>()
        runBlocking {
            getValue.addAll(getStringSetImpl(key, defValues))
        }
        if (debug) Log.d(TAG, "get: $key=$getValue")
        return getValue
    }

    private suspend fun putIntImpl(key: String?, value: Int?) {
        if (key?.isNotEmpty() == true && value != null) {
            val preferencesKey = intPreferencesKey(key)
            withContext(Dispatchers.IO){
                dataStore.edit {
                    it[preferencesKey] = value
                }
            }
        }
    }

    private suspend fun getIntImpl(key: String?, defaultValue: Int?): Int {
        return if (key?.isNotEmpty() == true) {
            val preferencesKey = intPreferencesKey(key)
            dataStore.data.map {
                it[preferencesKey] ?: (defaultValue ?: 0)
            }.first()
        } else {
            0
        }
    }

    private suspend fun putLongImpl(key: String?, value: Long?) {
        if (key?.isNotEmpty() == true && value != null) {
            val preferencesKey = longPreferencesKey(key)
            withContext(Dispatchers.IO){
                dataStore.edit {
                    it[preferencesKey] = value
                }
            }
        }
    }

    private suspend fun getLongImpl(key: String?, defaultValue: Long?): Long {
        return if (key?.isNotEmpty() == true) {
            val preferencesKey = longPreferencesKey(key)
            dataStore.data.map {
                it[preferencesKey] ?: (defaultValue ?: 0L)
            }.first()
        } else {
            0L
        }
    }

    private suspend fun putFloatImpl(key: String?, value: Float?) {
        if (key?.isNotEmpty() == true && value != null) {
            val preferencesKey = floatPreferencesKey(key)
            withContext(Dispatchers.IO){
                dataStore.edit {
                    it[preferencesKey] = value
                }
            }
        }
    }

    private suspend fun getFloatImpl(key: String?, defaultValue: Float?): Float {
        return if (key?.isNotEmpty() == true) {
            val preferencesKey = floatPreferencesKey(key)
            dataStore.data.map {
                it[preferencesKey] ?: (defaultValue ?: 0f)
            }.first()
        } else {
            0f
        }
    }

    private suspend fun putBooleanImpl(key: String?, value: Boolean?) {
        if (key?.isNotEmpty() == true && value != null) {
            val preferencesKey = booleanPreferencesKey(key)
            withContext(Dispatchers.IO){
                dataStore.edit {
                    it[preferencesKey] = value
                }
            }
        }
    }

    private suspend fun getBooleanImpl(key: String?, defaultValue: Boolean?): Boolean {
        return if (key?.isNotEmpty() == true) {
            val preferencesKey = booleanPreferencesKey(key)
            dataStore.data.map {
                it[preferencesKey] ?: (defaultValue ?: false)
            }.first()
        } else {
            false
        }
    }

    private suspend fun putStringImpl(key: String?, value: String?) {
        if (key?.isNotEmpty() == true && value?.isNotEmpty() == true) {
            val preferencesKey = stringPreferencesKey(key)
            withContext(Dispatchers.IO){
                dataStore.edit {
                    it[preferencesKey] = value
                }
            }
        }
    }

    private suspend fun getStringImpl(key: String?, defaultValue: String?): String {
        return if (key?.isNotEmpty() == true) {
            val preferencesKey = stringPreferencesKey(key)
            dataStore.data.map {
                it[preferencesKey] ?: (defaultValue ?: "")
            }.first()
        } else {
            ""
        }
    }

    private suspend fun putStringSetImpl(key: String?, value: Set<String>?) {
        if (key?.isNotEmpty() == true && value?.isNotEmpty() == true) {
            val preferencesKey = stringSetPreferencesKey(key)
            withContext(Dispatchers.IO){
                dataStore.edit {
                    it[preferencesKey] = value
                }
            }
        }
    }

    private suspend fun getStringSetImpl(key: String?, defaultValue: Set<String>?): Set<String> {
        return if (key?.isNotEmpty() == true) {
            val preferencesKey = stringSetPreferencesKey(key)
            dataStore.data.map {
                it[preferencesKey] ?: (defaultValue ?: setOf())
            }.first()
        } else {
            setOf()
        }
    }
}