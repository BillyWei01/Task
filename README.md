
## Task
[ ![Download](https://api.bintray.com/packages/horizon757/maven/Task/images/download.svg) ](https://bintray.com/horizon757/maven/Task/_latestVersion)

AsyncTask Plus

## New Feature
1、More concurrency control <br/>
2、Support Priority <br/>
3、Support Task grouping <br/>
4、Support lifecycle<br/>
5、Support chain invocation

## Download
```gradle
dependencies {
    implementation 'com.horizon.task:task:1.0.4'
}
```

## Prepare

1. Initialization(optional）

```kotlin
LogProxy.init(object : TaskLogger {
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG

    override fun e(tag: String, e: Throwable) {
        Log.e(tag, e.message, e)
    }
})
```

2. Notify Lifecycle Events

```kotlin
abstract class BaseActivity : Activity() {
    override fun onDestroy() {
        super.onDestroy()
        LifecycleManager.notify(this, LifeEvent.DESTROY)
    }

    override fun onPause() {
        super.onPause()
        LifecycleManager.notify(this, LifeEvent.HIDE)
    }

    override fun onResume() {
        super.onResume()
        LifecycleManager.notify(this, LifeEvent.SHOW)
    }
}
```


## How to use
### 1、Standard usage

Just like AsyncTask:

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        // ...
        TestTask()
                .priority(Priority.IMMEDIATE)
                .host(this)
                .execute("hello")
    }

    private inner class TestTask: UITask<String, Integer, String>(){
        override fun generateTag(): String {
            // Normally, you don't need to override this function
            return "custom tag"
        }

        override fun onPreExecute() {
            result_tv.text = "running"
        }

        override fun doInBackground(vararg params: String): String? {
            for (i in 0..100 step 2) {
                Thread.sleep(10)
                publishProgress(Integer(i))
            }
           return "result is：" + params[0].toUpperCase()
        }

        override fun onProgressUpdate(vararg values: Integer) {
            val progress = values[0]
            progress_bar.progress = progress.toInt()
            progress_tv.text = "$progress%"
        }

        override fun onPostExecute(result: String?) {
            result_tv.text = result
        }

        override fun onCancelled() {
            showTips("Task cancel ")
        }
    }
```


### 2、Executor

```
    TaskCenter.io.execute{
        // do something
    }

    TaskCenter.laneIO.execute("tag", {
        // do something
    })

    val serialExecutor = PipeExecutor(1)
    serialExecutor.execute{
        // do something
    }

    TaskCenter.serial.execute ("your tag", {
        // do something
    })
```

### 3、For RxJava

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

### 4、Chain invocation
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    val task = ChainTask<Double, Int, String>()
    task.tag("ChainTest")
        .preExecute { result_tv.text = "running" }
        .background { params ->
            for (i in 0..100 step 2) {
                // do something
                task.publishProgress(i)
            }
            "result is：" + (params[0] * 100)
        }
        .progressUpdate { values ->
            val progress = values[0]
            progress_bar.progress = progress
            progress_tv.text = "$progress%"
        }
        .postExecute { result_tv.text = it }
        .cancel { showTips("ChainTask cancel") }
        .priority(Priority.IMMEDIATE)
        .host(this)
        .execute(3.14)
}
```

## Link
https://www.jianshu.com/p/8afb6cf64eec

## License
See the [LICENSE](LICENSE.md) file for license rights and limitations.


