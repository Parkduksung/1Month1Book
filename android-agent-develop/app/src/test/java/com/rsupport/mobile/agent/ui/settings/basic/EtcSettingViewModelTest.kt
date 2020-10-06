package com.rsupport.mobile.agent.ui.settings.basic

import androidx.lifecycle.Observer
import base.BaseTest
import com.rsupport.mobile.agent.ui.base.ViewState
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EtcSettingViewModelTest : BaseTest() {

    override fun createModules(): List<Module> {
        return emptyList()
    }

    @Mock
    lateinit var viewStateOvserver: Observer<ViewState>

    lateinit var etcSettingViewModel: EtcSettingViewModel

    @Before
    override fun setup() {
        super.setup()
        etcSettingViewModel = EtcSettingViewModel(application)
    }

    // 의견보내기 선택시 이벤트 확인
    @Test
    fun feedBackClickTest() {
        etcSettingViewModel.viewState.observeForever(viewStateOvserver)
        etcSettingViewModel.onClickFeedBack()
        Mockito.verify(viewStateOvserver).onChanged(EtcSettingViewModel.EtcSettingViewState.FeedBackViewState)
    }

    // 웹사이트 이동 선택시 이벤트확인
    @Test
    fun goWebSiteClickTest() {
        etcSettingViewModel.viewState.observeForever(viewStateOvserver)
        etcSettingViewModel.onClickGoWebSite()
        Mockito.verify(viewStateOvserver).onChanged(EtcSettingViewModel.EtcSettingViewState.GoWebSiteViewState)
    }
}