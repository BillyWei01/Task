
# Task
A thread scheduler framework.


## feature
1. Support priority；
2. Support cancel task when Activity/Fragment destroy；
3. Support auto change priority when Activity/Fragment switch visible/invisible.
4. Filter duplicate tasks.
4. More control of task schedule.


# How to Use

At first, init logger
```kotlin
LogProxy.init(object : TaskLogger {
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG

    override fun e(tag: String, e: Throwable) {
        Log.e(tag, e.message, e)
    }
})
```

If you need lifecycle support, notify events when Activity/Fragment lifecycle change.

```kotlin
abstract class BaseActivity : Activity() {
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


There's several ways to use:

## 1、Just run jobs with executor
```kotlin
TaskCenter.io.execute{
    // do something
}
```


## 2、 Use like AsyncTask, but more contral
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

## 3、Use to RxJava
```kotlin
object TaskSchedulers {
    val io: Scheduler by lazy { Schedulers.from(TaskCenter.io) }
    val computation: Scheduler by lazy { Schedulers.from(TaskCenter.computation) }
    val single by lazy { Schedulers.from(PipeExecutor(1)) }
}
```

```kotlin
Observable.range(1, 8)
       .subscribeOn(TaskSchedulers.computation)
       .subscribe { Log.d(tag, "number:$it") }
```

# License
See the [LICENSE](LICENSE.md) file for license rights and limitations.


