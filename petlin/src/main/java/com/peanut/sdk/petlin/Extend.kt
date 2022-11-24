package com.peanut.sdk.petlin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.util.Base64
import android.widget.Toast
import java.io.File

object Extend{
    /**
     * 将长整型byte数描述为文件大小, 最大支持显示TB，保留两位小数。
     * @param separator 用于分隔小数与单位
     */
    fun Long.describeAsFileSize(separator: String = " "): String {
        return when {
            this.shr(40) >= 1.0 -> String.format("%.2f", this / 1_099_511_627_776) + "${separator}TB"
            this.shr(30) >= 1.0 -> String.format("%.2f", this / 1_073_741_824.0) + "${separator}GB"
            this.shr(20) >= 1.0 -> String.format("%.2f", this / 1_048_576.0) + "${separator}MB"
            this.shr(10) >= 1.0 -> String.format("%.2f", this / 1024.0) + "${separator}KB"
            else -> String.format("%.2f ", this / 1.0) + "${separator}B"
        }
    }

    /**
     * 将整型秒数描述为持续时间, 最大支持显示小时。
     * @param hour 小时单位描述，默认为 _小时_
     * @param minute 分钟单位描述，默认为 _分_
     * @param seconds 秒单位描述，默认为 _秒
     */
    fun Int.describeAsTimeLasts(hour: String = " 小时 ", minute: String = " 分 ", seconds: String = " 秒"): String {
        val hh = this / 3600
        val mm = this % 3600 / 60
        val ss = this % 3600 % 60
        return when {
            hh > 0 -> "$hh$hour${"0".repeat(if (mm < 10) 1 else 0)}$mm$minute${"0".repeat(if (ss < 10) 1 else 0)}$ss$seconds"
            mm > 0 -> "$mm$minute${"0".repeat(if (ss < 10) 1 else 0)}$ss$seconds"
            else -> "$ss$seconds"
        }
    }

    /**
     * 显示一个Toast, 支持在任意线程发送
     * @param duration 显示时长，仅支持 [Toast.LENGTH_SHORT]或[Toast.LENGTH_LONG]
     */
    fun String.toast(context: Context, duration: Int = Toast.LENGTH_SHORT) {
        if (Thread.currentThread() == context.mainLooper.thread) {
            Toast.makeText(context, this, duration).show()
        }else{
            Handler(context.mainLooper).post {
                Toast.makeText(context, this, duration).show()
            }
        }
    }

    /**
     * 计算某个颜色是否为亮色，用于判断文字颜色等
     * @param threshold 阈值，大于为亮色
     * @return 是否为亮色，亮色应该用黑色文字
     */
    fun Int.isLightColor(threshold: Float = 0.5f): Boolean {
        return try {
            val r = this shr 16 and 0xff
            val g = this shr 8 and 0xff
            val b = this and 0xff
            (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        } > threshold
    }

    /**
     * 复制文本到剪切板
     */
    fun String.copy(context: Context, label: String = context.packageName) {
        val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label, this)
        clipboard?.setPrimaryClip(clip)
    }

    /**
     * 使用Base 64编码文本
     */
    fun String.encodeBase64(vararg flags: Int): String {
        var flag = 0
        flags.forEach {
            flag = flag or it
        }
        return Base64.encodeToString(this.toByteArray(), flag)
    }

    /**
     * 从路径获取文件名(包含后缀)
     */
    fun String.getFileName() = File(this).name
    /**
     * 从路径获取文件名(不包含后缀)
     */
    fun String.getFileNameWithoutExt() = File(this).nameWithoutExtension
}