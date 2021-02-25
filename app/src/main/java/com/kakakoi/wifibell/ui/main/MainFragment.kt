package com.kakakoi.wifibell.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kakakoi.wifibell.R
import com.kakakoi.wifibell.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        val signalLevelObserver = Observer<Int> { level ->
            // Update the UI, in this case, a TextView.
            binding.signalLevelText.setTextColor(Color.rgb(level, 0, 0))
            if (level > 70) {
                binding.pingImage.imageTintList =
                    context?.getColorStateList(R.color.ping_color_state_list)
                Log.d(TAG, "onCreateView: level > 70")
            } else {
                binding.pingImage.imageTintList =
                    context?.getColorStateList(R.color.ping_color_warn_state_list)
                Log.d(TAG, "onCreateView: level < 70")
            }
        }
        viewModel.signalLevel.observe(viewLifecycleOwner, signalLevelObserver)

        return binding.root
    }
}