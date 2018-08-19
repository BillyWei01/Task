package com.horizon.taskdemo.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.horizon.task.IOTask
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import com.horizon.taskdemo.base.HttpClient
import okhttp3.Request


class LoadingTestActivity : BaseActivity() {
    private lateinit var mTestIv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_test)
        mTestIv = findViewById(R.id.test_iv)

        val url = "https://pic1.zhimg.com/80/63536f2f01409f750162828a980a0380_hd.jpg"
        LoadingTask().setHost(this).execute(url)
    }

    private inner class LoadingTask : IOTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap? {
            val url = params[0]
            val builder = Request.Builder().url(url)
            try {
                val response = HttpClient.execute(builder.build())
                if (response.isSuccessful) {
                    return BitmapFactory.decodeStream(response.body()!!.byteStream())
                }
            } catch (e: Exception) {
                Log.e(tag, e.message, e)
            }

            return null
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                mTestIv.setImageBitmap(result)
            }
        }
    }
}
