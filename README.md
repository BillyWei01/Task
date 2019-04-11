
## Task
[ ![Download](https://api.bintray.com/packages/horizon757/maven/Task/images/download.svg) ](https://bintray.com/horizon757/maven/Task/_latestVersion)

Task 可以简单地理解为“加强版的AsyncTask”（其实也不仅仅是AsyncTask）。

## 特性
相比AsyncTask，新增了以下特性：<br/>
1、更灵活的并发控制<br/>
2、支持调度优先级<br/>
3、支持任务去重<br/>
4、支持生命周期（onDestroy时取消任务，自动调整优先级）

## 下载
```gradle
dependencies {
    implementation 'com.horizon.task:task:1.0.4'
}
```

## 用法
首先，初始化日志接口
```kotlin
LogProxy.init(object : TaskLogger {
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG

    override fun e(tag: String, e: Throwable) {
        Log.e(tag, e.message, e)
    }
})
```

如果需要生命周期支持,在 Activity/Fragment 生命周期回调中通知事件。

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

然后，具体执行任务，有几种方法：

### 常规用法
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
            // 一般情况下不需要重写这个函数，这里只是为了演示
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

UITask和AsyncTask用法是类似的, 只是多了一些API：
- 因为生命需要观察Activity的生命周期，所以需要调用host()，传入当前Activity
- 可以设置任务优先级
- 有必要时可以重写generateTag来自定义任务的tag

### Executor
当然，项目中不仅仅是UITask，TaskCenter，以及各种Executor,  都是可以单独使用的。
比方说只是想简单地执行任务，不需要和UI交互，也可以直接使用Executor：
```
    TaskCenter.io.execute{
        // do something
    }

    TaskCenter.laneIO.execute("laneIO", {
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

### For RxJava
很多开源项目都设计了API来使用外部的Executor，例如RxJava的话可以这样使用：
```kotlin
object TaskSchedulers {
    val io: Scheduler by lazy { Schedulers.from(TaskCenter.io) }
    val computation: Scheduler by lazy { Schedulers.from(TaskCenter.computation) }
    val single by lazy { Schedulers.from(PipeExecutor(1)) }
}
```
使用：
```kotlin
Observable.range(1, 8)
       .subscribeOn(TaskSchedulers.computation)
       .subscribe { Log.d(tag, "number:$it") }
```
这样使用有一个好处：
项目自身的任务和第三方库的任务都在一个线程池上执行任务，可复用彼此创建线程。

### 链式调用
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    val task = ChainTask<Double, Int, String>()
    task.tag("ChainTest")
        .preExecute { result_tv.text = "running" }
        .background { params ->
            for (i in 0..100 step 2) {
                Thread.sleep(10)
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
        .cancel { showTips("ChainTask cancel ") }
        .priority(Priority.IMMEDIATE)
        .host(this)
        .execute(3.14)
}
```

## 相关阅读
https://www.jianshu.com/p/4dff25a87122

## License
See the [LICENSE](LICENSE.md) file for license rights and limitations.


