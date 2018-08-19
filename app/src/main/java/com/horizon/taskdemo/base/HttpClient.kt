package com.horizon.taskdemo.base

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException


object HttpClient{
    private var client: OkHttpClient? = null

    fun init(context: Context) {
        if (client == null) {
            val cacheDirPath = context.cacheDir.path + "/http/"
            client = OkHttpClient.Builder()
                    .cache(Cache(File(cacheDirPath), (64 shl 20).toLong()))
                    .build()
        }
    }

    @Throws(IOException::class)
    fun execute(request: Request): Response {
        return client!!.newCall(request).execute()
    }

}
