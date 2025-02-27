package com.example.lockdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat.setTranslationY
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.lockdemo.databinding.FragmentLockBinding
import kotlinx.coroutines.launch

/**
 * 锁屏 Fragment
 * Created by RoyYao on 2025/2/27
 */
class LockScreenFragment :
    Fragment() {
    private lateinit var binding: FragmentLockBinding
    private var offsetY = 0f
    private var isDragging = false
    private val viewModel: LockViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 设置触摸监听器 控制锁屏升降
        binding.root.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    offsetY = event.rawY - v.y
                    isDragging = true
                }

                MotionEvent.ACTION_MOVE -> if (isDragging) {
                    val y = event.rawY - offsetY
                    viewModel.translationFlow.value = y
                }

                MotionEvent.ACTION_UP -> isDragging = false
            }
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLockFlow.collect {
                if (it) {
                    binding.tvTip.text =
                        requireActivity().application.getString(R.string.slide_up)
                    binding.ivArrowUp.isVisible = true
                    binding.ivArrowDown.isVisible = false
                } else {
                    binding.tvTip.text =
                        requireActivity().application.getString(R.string.slide_down)
                    binding.ivArrowUp.isVisible = false
                    binding.ivArrowDown.isVisible = true
                }
            }
        }
    }

    companion object {
        val TAG: String = LockScreenFragment::class.java.simpleName
        const val SWIPE_DOWN_THRESHOLD = 500 // 下滑锁屏需要距离
        const val SWIPE_UP_THRESHOLD = 200 // 上滑解锁需要距离
    }
}