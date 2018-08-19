
# Task
A thread framework written in Kotlin.
Include a multipurpose executor family and an extension of AsyncTask.

## feature
1. Support priority；
2. Support cancel task when Activity/Fragment destroy；
3. Support auto change priority when Activity/Fragment switch visible/invisible.
4. Filter duplicate tasks.
4. More control of task schedule.


# How to Use

## 1、dispatch events

```kotlin
abstract class BaseActivity : Activity() {
    protected val tag = this.javaClass.simpleName!!

    override fun onDestroy() {
        super.onDestroy()
        LifecycleManager.notify(this, Event.DESTROY)
    }

    override fun onPause() {
        super.onPause()
        LifecycleManager.notify(this, Event.HIDE)
    }

    override fun onResume() {
        super.onResume()
        LifecycleManager.notify(this, Event.SHOW)
    }
}
```

## 2、init logger
```kotlin
LogProxy.init(object : TaskLogger {
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG

    override fun e(tag: String, e: Throwable) {
        Log.e(tag, e.message, e)
    }
})
```


## 3、Use case: just like AsyncTask, but more contral
```kotlin
class TestActivity : BaseActivity() {
    private lateinit var mCountingTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counting_test)

        mCountingTv = findViewById(R.id.counting_tv)

        CountingTask()
                .setHost(this)
                .setPriority(Priority.IMMEDIATE)
                .execute(20 as Integer)
    }

    private inner class CountingTask : IOTask<Integer, String, String>(){
        override fun doInBackground(vararg params: Integer): String? {
            val n = params[0] as Int
            return try {
                for (count in n downTo 1) {
                    publishProgress(Integer.toString(count))
                    Thread.sleep(1000)
                }
                "done"
            } catch (e: InterruptedException) {
                Log.w(tag, e.javaClass.simpleName + " occurred")
                "cancel"
            }
        }

        override fun onProgressUpdate(vararg values: String) {
            mCountingTv.text = values[0]
        }

        override fun onCancelled(result: String?) {
            Log.i(tag, "task result:  " + result!!)
        }

        override fun onPostExecute(result: String?) {
            mCountingTv.text = result
        }
    }
}
```

## 4、It's ok to run jobs with executor
```kotlin
TaskCenter.io.execute{
    // do something
}
```

# License
See the [LICENSE](LICENSE.md) file for license rights and limitations.


