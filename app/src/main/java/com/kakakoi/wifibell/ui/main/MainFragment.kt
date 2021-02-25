package com.kakakoi.wifibell.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakakoi.wifibell.R
import com.kakakoi.wifibell.databinding.MainFragmentBinding
import com.kakakoi.wifibell.model.PingSound


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding
    private lateinit var sound: PingSound

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        val signalLevelObserver = Observer<Int> { level ->
            binding.signalLevelText.setTextColor(Color.rgb(level, 0, 0))
            if (level > viewModel.SIGNAL_LEVEL / 2) {
                binding.pingImage.imageTintList =
                    context?.getColorStateList(R.color.ping_color_state_list)
                binding.statusImage.imageTintList =
                    context?.getColorStateList(R.color.ping_color_state_list)
                viewModel.sound.stop()
                Log.d(TAG, "onCreateView: level > 70")
            } else {
                binding.pingImage.imageTintList =
                    context?.getColorStateList(R.color.ping_color_warn_state_list)
                binding.statusImage.imageTintList =
                    context?.getColorStateList(R.color.ping_color_warn_state_list)
/*
                val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(5000,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                    vibrator.vibrate(vibrationEffect)
                } else {
                    vibrator.vibrate(5000)
                }

 */
                viewModel.sound.play()

                Log.d(TAG, "onCreateView: level < 70")
            }
            animate(binding.pingImage)
        }
        viewModel.signalLevel.observe(viewLifecycleOwner, signalLevelObserver)

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sound.close()
    }

    private fun animate(img: ImageView) {
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            R.anim.scale_anim
        )
        binding.pingImage.startAnimation(animation)
    }
}