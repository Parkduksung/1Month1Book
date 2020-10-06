package com.rsupport.mobile.agent.ui.settings.basic

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rsupport.mobile.agent.BuildConfig
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.databinding.FragmentBasicSettingDefaultBinding
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.about.LicenceActivity
import com.rsupport.mobile.agent.ui.base.BaseViewModel
import com.rsupport.mobile.agent.ui.base.ViewState
import com.rsupport.mobile.agent.ui.faq.FAQActivity
import com.rsupport.mobile.agent.ui.notice.NoticeActivity
import com.rsupport.mobile.agent.ui.terms.TermsActivity
import com.rsupport.mobile.agent.ui.tutorial.TutorialActivity
import com.rsupport.mobile.agent.ui.tutorial.TutorialSelectActivity
import org.koin.java.KoinJavaComponent.inject


@Keep
class DefaultSettingFragment : Fragment() {

    private lateinit var dataBinding: FragmentBasicSettingDefaultBinding
    private val defaultSettingViewModel: DefaultSettingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_basic_setting_default, container, false)
        DataBindingUtil.bind<FragmentBasicSettingDefaultBinding>(view)?.apply {
            dataBinding = this
            viewModel = defaultSettingViewModel
        }
        return view
    }

    override fun onPause() {
        defaultSettingViewModel.viewState.removeObservers(viewLifecycleOwner)
        super.onPause()
    }

    override fun onResume() {
        defaultSettingViewModel.viewState.observe(viewLifecycleOwner, Observer {
            (it as? DefaultSettingViewModel.DefaultSettingViewState)?.let { viewState ->
                onViewStateChanged(viewState)
            }
        })
        super.onResume()
    }

    private fun onViewStateChanged(viewState: DefaultSettingViewModel.DefaultSettingViewState) = when (viewState) {
        DefaultSettingViewModel.DefaultSettingViewState.LicenseViewState -> startLicenseActivity()
        DefaultSettingViewModel.DefaultSettingViewState.TutorialViewState -> startTutorialActivity()
        DefaultSettingViewModel.DefaultSettingViewState.TermsViewState -> startTermsActivity()
        DefaultSettingViewModel.DefaultSettingViewState.PrivacyViewState -> startPrivacyActivity()
        DefaultSettingViewModel.DefaultSettingViewState.NoticeViewState -> startNoticeActivity()
        DefaultSettingViewModel.DefaultSettingViewState.FaqViewState -> startFaqActivity()
    }

    private fun startTermsActivity() {
        val intent = Intent(activity, TermsActivity::class.java)
        intent.putExtra("TERMS", true)
        startActivity(intent)
    }

    private fun startPrivacyActivity() {
        val intent = Intent(activity, TermsActivity::class.java)
        intent.putExtra("TERMS", false)
        startActivity(intent)
    }

    private fun startFaqActivity() {
        val intent = Intent(activity, FAQActivity::class.java)
        intent.putExtra("about", true)
        startActivity(intent)
    }

    private fun startNoticeActivity() {
        val intent = Intent(activity, NoticeActivity::class.java)
        intent.putExtra("about", true)
        startActivity(intent)
    }

    private fun startTutorialActivity() {
        val intent = Intent(activity, TutorialActivity::class.java)
        intent.putExtra("about", true)
        intent.putExtra(TutorialSelectActivity.TUTORIAL_TYPE, TutorialSelectActivity.TUTORIAL_TYPE_CORP)
        intent.putExtra("AGENT_CALL", true)
        startActivity(intent)
    }

    private fun startLicenseActivity() {
        val intent = Intent(activity, LicenceActivity::class.java)
        intent.putExtra("AGENT_CALL", true)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

class DefaultSettingViewModel(app: Application) : BaseViewModel(app) {

    private val configRepository: ConfigRepository by inject(ConfigRepository::class.java)
    val version by lazy {
        ObservableField<String>(BuildConfig.VERSION_NAME)
    }

    val hasNewNotice: Boolean
        get() = configRepository.hasNewNotice()

    fun onClickLicense() {
        viewStateChange(DefaultSettingViewState.LicenseViewState)
    }

    fun onClickTutorial() {
        viewStateChange(DefaultSettingViewState.TutorialViewState)
    }

    fun onClickTerms() {
        viewStateChange(DefaultSettingViewState.TermsViewState)
    }

    fun onClickPrivacy() {
        viewStateChange(DefaultSettingViewState.PrivacyViewState)
    }

    fun onClickNotice() {
        viewStateChange(DefaultSettingViewState.NoticeViewState)
    }

    fun onClickFaq() {
        viewStateChange(DefaultSettingViewState.FaqViewState)
    }

    sealed class DefaultSettingViewState : ViewState {
        object LicenseViewState : DefaultSettingViewState()
        object TutorialViewState : DefaultSettingViewState()
        object TermsViewState : DefaultSettingViewState()
        object PrivacyViewState : DefaultSettingViewState()
        object NoticeViewState : DefaultSettingViewState()
        object FaqViewState : DefaultSettingViewState()
    }
}