package com.horizon.taskdemo.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import com.horizon.taskdemo.R
import com.horizon.taskdemo.base.BaseActivity
import com.horizon.taskdemo.base.TaskSchedulers
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), View.OnClickListener {
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loading_btn.setOnClickListener(this)
        counting_btn.setOnClickListener(this)
        serial_btn.setOnClickListener(this)
        concurrent_btn.setOnClickListener(this)
        chain_btn.setOnClickListener(this)

        disposable = Observable.range(1, 8)
                .flatMap { i ->
                    Observable.just(i)
                            .subscribeOn(TaskSchedulers.computation)
                            .map { Thread.sleep(1050); "param:$it" }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    Log.d(tag, result)
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.loading_btn -> startActivity(LoadingTestActivity::class.java)
            R.id.counting_btn -> startActivity(CountingTestActivity::class.java)
            R.id.serial_btn -> startActivity(SerialTestActivity::class.java)
            R.id.concurrent_btn -> startActivity(ConcurrentTestActivity::class.java)
            R.id.chain_btn -> startActivity(ChainTestActivity::class.java)
            //R.id.chain_btn -> startActivity(NotChainTestActivity::class.java)
        }
    }

}

