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
}