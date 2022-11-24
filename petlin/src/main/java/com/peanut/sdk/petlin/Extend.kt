package com.peanut.sdk.petlin

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
}