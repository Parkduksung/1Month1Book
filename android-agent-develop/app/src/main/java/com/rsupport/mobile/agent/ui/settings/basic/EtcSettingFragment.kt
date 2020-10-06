package com.rsupport.mobile.agent.ui.settings.basic

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.databinding.FragmentBasicSettingEtcBinding
import com.rsupport.mobile.agent.ui.base.BaseViewModel
import com.rsupport.mobile.agent.ui.base.ViewState
import org.koin.java.KoinJavaComponent.inject

@Keep
class EtcSettingFragment : Fragment() {

    private val etcSettingViewModel by inject(EtcSettingViewModel::class.java)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_basic_setting_etc, container, false)
        DataBindingUtil.bind<FragmentBasicSettingEtcBinding>(view)?.apply {
            viewModel = etcSettingViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return view
    }

    override fun onResume() {
        etcSettingViewModel.viewState.observe(viewLifecycleOwner, Observer {
            (it as? EtcSettingViewModel.EtcSettingViewState)?.let { viewState -> onViewStateChanged(viewState) }
        })
        super.onResume()
    }

    private fun onViewStateChanged(viewState: EtcSettingViewModel.EtcSettingViewState) = when (viewState) {
        EtcSettingViewModel.EtcSettingViewState.FeedBackViewState -> startMailIntextToCompany()
        EtcSettingViewModel.EtcSettingViewState.GoWebSiteViewState -> startWebBrowser(resources.getString(R.string.mailtofriend_homepage))
    }

    override fun onPause() {
        etcSettingViewModel.viewState.removeObservers(viewLifecycleOwner)
        super.onPause()
    }

    private fun getCommnetFeedback(): String? {
        var ret: String? = ""
        ret = getString(R.string.mailtofriend_comment_word).toString() + " \n\n\n\n"
        ret += getString(R.string.msg_sendfrom)
        return ret
    }

    private fun startMailIntextToCompany() {
        val feedBackResId = R.string.mailfeedback_to
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(feedBackResId)))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailfeedback_subject))
        emailIntent.putExtra(Intent.EXTRA_TEXT, getCommnetFeedback())
        startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }

    private fun startWebBrowser(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val url = Uri.parse(link)
        intent.data = url
        startActivity(intent)
    }
}

class EtcSettingViewModel(app: Application) : BaseViewModel(app) {
    fun onClickFeedBack() {
        viewStateChange(EtcSettingViewState.FeedBackViewState)
    }

    fun onClickGoWebSite() {
        viewStateChange(EtcSettingViewState.GoWebSiteViewState)
    }

    sealed class EtcSettingViewState : ViewState {
        object FeedBackViewState : EtcSettingViewState()
        object GoWebSiteViewState : EtcSettingViewState()
    }
}