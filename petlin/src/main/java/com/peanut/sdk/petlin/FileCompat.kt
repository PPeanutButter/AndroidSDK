package com.peanut.sdk.petlin

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.peanut.sdk.petlin.Extend.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*


object FileCompat {
    private const val TAG = "FileCompat"
    private const val WRITE_EXTERNAL_STORAGE_CODE = 0x0010

    /**
     * 复制文件：Uri到String
     */
    suspend fun copyFile(source: Uri, destination: String, context: Context, debug: Boolean = false): Boolean {
        return withContext(Dispatchers.IO) {
            when (source.scheme) {
                "file" -> {
                    val start = System.currentTimeMillis()
                    val fis = FileInputStream(source.toFile())
                    val fos = FileOutputStream(destination)
                    try {
                        copyFileUseStream(fis, fos)
                        if (debug) {
                            Log.d(TAG, "copyFile: copy mode file cost ${System.currentTimeMillis() - start} ms")
                        }
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    } finally {
                        fis.close()
                        fos.apply { this.flush() }.close()
                    }
                }
                "content" -> {
                    try {
                        val start = System.currentTimeMillis()
                        val fileDescriptor = context.contentResolver.openFileDescriptor(source, "r")
                        if (fileDescriptor != null) {
                            val fd = fileDescriptor.fileDescriptor
                            val fis = FileInputStream(fd)
                            val fos = FileOutputStream(destination)
                            try {
                                copyFileUseStream(fis, fos)
                                if (debug) {
                                    Log.d(TAG, "copyFile: copy mode file cost ${System.currentTimeMillis() - start} ms")
                                }
                                return@withContext true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@withContext false
                            } finally {
                                fis.close()
                                fos.apply { this.flush() }.close()
                                fileDescriptor.close()
                            }
                        } else {
                            Log.d(TAG, "copyFile: the provider recently crashed")
                            false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                else -> {
                    "不支持的协议:${source.scheme}".toast(context)
                    false
                }
            }
        }
    }

    /**
     * 复制文件：fileInputStream -> fileOutputStream
     */
    suspend fun copyFileUseStream(fileInputStream: InputStream, fileOutputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val buffer = ByteArray(4096)
            var byteRead: Int
            while (-1 != fileInputStream.read(buffer).also { byteRead = it }) {
                fileOutputStream.write(buffer, 0, byteRead)
            }
            fileOutputStream.flush()
        }
    }

    /**
     * 获取文件名与大小
     */
    suspend fun getFileNameAndSize(context: Context, uri: Uri, withoutExtension: Boolean = false): Pair<String, Long> {
        when (uri.scheme) {
            "file" -> {
                val file = uri.toFile()
                return (if (withoutExtension) file.nameWithoutExtension else file.name) to file.length()
            }
            "content" -> {
                return withContext(Dispatchers.IO) {
                    try {
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (cursor.moveToFirst()) {
                                val result = (if (withoutExtension) cursor.getString(nameIndex).substring(
                                    0,
                                    cursor.getString(nameIndex).lastIndexOf(".")
                                ) else cursor.getString(nameIndex)) to cursor.getLong(sizeIndex)
                                cursor.close()
                                result
                            } else {
                                cursor.close()
                                "无法查询到文件名与大小(cursor is empty)" to 0L
                            }
                        } else {
                            "无法查询到文件名与大小(系统未索引该文件)" to 0L
                        }
                    } catch (e: Exception) {
                        "无法查询到文件名与大小(系统未索引该文件)" to 0L
                    }
                }
            }
            else -> return "不支持的协议:${uri.scheme}" to 0L
        }
    }

    /**
     * # 将文件保存在MediaStore.Downloads目录下
     * @param context 用于创建contentResolver
     * @param dir 文件夹名，一般是应用的英文名
     * @param fileName DISPLAY_NAME，不能有路径
     * @param mimeType MimeType默认application/octet-stream
     * @param requestWritePermissionForMe 是否需要为你申请动态权限，否则检测到没权限就放弃
     * @param onCopyFile 写文件的操作，在Dispatchers.IO线程中
     */
    suspend fun saveFileToPublicDownload(
        context: Activity, dir: String,
        fileName: String, debug: Boolean = false,
        mimeType: String = "application/octet-stream",
        requestWritePermissionForMe: Boolean = false,
        onCopyFile: suspend (FileOutputStream) -> Unit
    ) {
        val start = System.currentTimeMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            /**
             * # Android 10 +
             * [Official](https://developer.android.com/training/data-storage/shared/media?hl=zh-cn)
             * 在搭载 Android 10 或更高版本的设备上，您无需任何存储相关权限即可访问和修改您的应用拥有的媒体文件
             * 包括 MediaStore.Downloads
             */
            val resolver: ContentResolver = context.contentResolver
            // content://media/external_primary/downloads
            val downloadCollection: Uri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val fileDetails = ContentValues().apply {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + dir)
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                //文件独占
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            withContext(Dispatchers.IO) {
                //创建文件
                val destUri = resolver.insert(downloadCollection, fileDetails) ?: return@withContext false
                try {
                    //获取描述符用于写入文件
                    val pfd = resolver.openFileDescriptor(destUri, "w", null) ?: return@withContext false
                    val fos = FileOutputStream(pfd.fileDescriptor)
                    onCopyFile(fos)
                    fos.flush()
                    fos.close()
                    pfd.close()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                } finally {
                    //update state
                    fileDetails.clear()
                    fileDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(destUri, fileDetails, null, null)
                }
            }
            if (debug) {
                Log.d(TAG, "saveFileToPublicDownload: for Android 10 +, cost ${System.currentTimeMillis() - start} ms")
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /**
             * # Android 6.0 - Android 9.0
             * 动态权限申请
             */
            val hasWriteStoragePermission: Int = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                withContext(Dispatchers.IO) {
                    try {
                        val fos = FileOutputStream(
                            File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                                        + File.separator
                                        + dir
                                        + File.separator
                                        + fileName
                            )
                        )
                        onCopyFile(fos)
                        fos.flush()
                        fos.close()
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                if (debug) {
                    Log.d(TAG, "saveFileToPublicDownload: for Android 6 - 9, cost ${System.currentTimeMillis() - start} ms")
                }
            } else {
                if (requestWritePermissionForMe)
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_STORAGE_CODE)
                else {
                    "没有存储写入权限".toast(context)
                }
            }
        } else {
            /**
             * # Android 6.0 -
             * 直接写入
             */
            withContext(Dispatchers.IO) {
                try {
                    val fos = FileOutputStream(
                        File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                                    + File.separator
                                    + dir
                                    + File.separator
                                    + fileName
                        )
                    )
                    onCopyFile(fos)
                    fos.flush()
                    fos.close()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            if (debug) {
                Log.d(TAG, "saveFileToPublicDownload: for Android 6 -, cost ${System.currentTimeMillis() - start} ms")
            }
        }
    }

}
