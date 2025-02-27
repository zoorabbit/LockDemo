package com.example.lockdemo

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.lockdemo.LockScreenFragment.Companion.SWIPE_DOWN_THRESHOLD
import com.example.lockdemo.LockScreenFragment.Companion.SWIPE_UP_THRESHOLD
import com.example.lockdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 锁屏 窗帘主页
 * Created by RoyYao on 2025/2/27
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: LockViewModel by viewModels()

    // 锁屏状态
    private var mIsLock = false

    // 上一次 偏移量
    private var lastTranslationY = 0f

    // 锁屏 偏移量 延时处理 Job
    private var timer: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val lockFragment = LockScreenFragment()
        supportFragmentManager.beginTransaction()
            .add(binding.fcvLock.id, lockFragment, LockScreenFragment.TAG).commitAllowingStateLoss()
        lifecycleScope.launch {
            viewModel.translationFlow.collect {
                setTranslationY(it)
            }
        }
    }

    // 设置 锁屏页面 偏移量
    private fun setTranslationY(offsetY: Float) {
        val lockView = binding.fcvLock
        val lockViewHeight: Int = lockView.height
        if (offsetY > lockViewHeight) {
            return
        }
        // 清除 之前延时任务
        timer?.cancel()
        if (lastTranslationY == 0f) {
            if (lockView.translationY == 0f && offsetY > 0) {
                return
            }
            lastTranslationY = lockView.translationY
        }
        lockView.translationY = lastTranslationY + offsetY
        // 100ms 后执行延时任务
        timer = lifecycle.coroutineScope.launch {
            delay(100)
            val isLock = offsetY > 0
            mIsLock = if (isLock) {
                // 下滑超过 500px 锁屏
                offsetY > SWIPE_DOWN_THRESHOLD
            } else {
                // 上滑超过 200px 解锁
                !(-offsetY > SWIPE_UP_THRESHOLD)
            }
            viewModel.isLockFlow.value = mIsLock
            lastTranslationY = 0f
            val curTranslationY: Float = lockView.translationY
            val end = (if (mIsLock) 0 else -lockViewHeight + 100).toFloat()
            if (curTranslationY == end) {
                return@launch
            }
            val animator = ValueAnimator.ofFloat(curTranslationY, end)
            animator.addUpdateListener { animation: ValueAnimator ->
                val value = animation.animatedValue as Float
                lockView.translationY = value
            }
            animator.setDuration(200)
            animator.start()
        }
    }
}