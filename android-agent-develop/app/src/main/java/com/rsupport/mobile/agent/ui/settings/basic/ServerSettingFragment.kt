package com.rsupport.mobile.agent.ui.settings.basic

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.databinding.FragmentBasicSettingServerBinding
import org.koin.java.KoinJavaComponent.inject

@Keep
class ServerSettingFragment : Fragment() {


    companion object {
        @JvmStatic
        @BindingAdapter(value = ["app:switchState"])
        fun switchState(view: View, switchState: Boolean?) {
            switchState?.let {
                if (it) {
                    view.setBackgroundResource(R.drawable.toggle_animation_off)
                } else {
                    view.setBackgroundResource(R.drawable.toggle_animation_on)
                }
                val frameAnimation = view.background as AnimationDrawable
                frameAnimation.start()
            }
        }
    }

    private val basicSettingViewModel by inject(ServerSettingViewModel::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_basic_setting_server, container, false)
        DataBindingUtil.bind<FragmentBasicSettingServerBinding>(view)?.apply {
            viewModel = basicSettingViewModel
            lifecycleOwner = this@ServerSettingFragment
        }
        return view
    }

    override fun onResume() {
        viewLifecycleOwner.lifecycle.addObserver(basicSettingViewModel)
        super.onResume()
    }

    override fun onPause() {
        viewLifecycleOwner.lifecycle.removeObserver(basicSettingViewModel)
        super.onPause()
    }
}