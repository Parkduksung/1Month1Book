package com.rsupport.mobile.agent.koin

import androidx.room.Room
import com.rsupport.android.engine.install.EngineContextFactory
import com.rsupport.android.engine.install.IEngineContext
import com.rsupport.android.engine.install.config.Configuration
import com.rsupport.android.engine.install.gson.dto.EngineGSon
import com.rsupport.android.engine.install.installer.IEngineInstaller
import com.rsupport.android.engine.install.installer.MarketSearch
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.knox.Updatable
import com.rsupport.litecam.binder.Binder
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.WebConnectionApiService
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.extension.isInstalledPkg
import com.rsupport.mobile.agent.modules.device.inject.EngineEventDispatcherFactory
import com.rsupport.mobile.agent.modules.device.inject.EventDispatcher
import com.rsupport.mobile.agent.modules.device.power.PowerKeyController
import com.rsupport.mobile.agent.modules.engine.EngineProviders
import com.rsupport.mobile.agent.modules.engine.KnoxKeyUpdate
import com.rsupport.mobile.agent.modules.sysinfo.SystemInfo
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.repo.agent.AgentRepository
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.dao.AgentDatabase
import com.rsupport.mobile.agent.repo.device.DeviceRepository
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.status.PrefStatusContainer
import com.rsupport.mobile.agent.ui.agent.EngineActivationViewModel
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.login.LoginInteractor
import com.rsupport.mobile.agent.ui.settings.basic.EtcSettingViewModel
import com.rsupport.mobile.agent.ui.settings.basic.ServerSettingInteractor
import com.rsupport.mobile.agent.ui.settings.basic.ServerSettingViewModel
import com.rsupport.mobile.agent.ui.settings.delete.AgentDeleteInteractor
import com.rsupport.mobile.agent.utils.*
import com.rsupport.util.log.RLog
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

class AppKoinSetup : KoinBaseSetup() {

    private val interactorModule = module {
        factory { AgentInfoInteractor().apply { initialized() } }
        factory { LoginInteractor() }
        factory { AgentDeleteInteractor() }
        factory { ServerSettingInteractor() }
    }

    private val viewModelModule = module {
        viewModel { ServerSettingViewModel(get(), get()) }
        viewModel { EtcSettingViewModel(get()) }
        viewModel { EngineActivationViewModel(get()) }
    }

    private val utilModule = module {
        single { SdkVersion() }
        single { NetworkUtils() }
        single { AgentLogManager() }
        single<ApiService> { WebConnectionApiService(get(), Global.getInstance().webConnection, get(), get()) }
        factory { ScreenUtils() }
    }

    private val databaseModule = module {
        single {
            Room.databaseBuilder(get(), AgentDatabase::class.java, "agent.db")
                    .fallbackToDestructiveMigration()
                    .build()
        }
        single {
            get<AgentDatabase>().getAgentDao()
        }
        single { AgentRepository() }
        single { DeviceRepository() }
        single { ConfigRepository() }
    }

    private val coreModule = module {
        factory<IEngineContext> {
            EngineContextFactory.createEngineContext(get(), EngineContextFactory.PRODUCT_ID_MOBI_ASP).apply {
                configuration.apply {
                    installPriority = Configuration.PRIORITY_INSTALL_MARKET
                    flag = Configuration.FLAG_PRIORITY_ONLY
                    setExcludeRspermFilter { installFileInfo: EngineGSon.InstallFileInfo ->
                        val marketSearch = MarketSearch(this.marketSort)
                        val marketTypes = marketSearch.getInstalledMarketType(get())
                        val supportedMarket = marketSearch.getSupportedMarket(marketTypes, installFileInfo)
                        // 마켓 다운로드만 지원하도록 filtering 한다.
                        if (supportedMarket == IEngineInstaller.TYPE_NONE) {
                            RLog.v("notSupportMarket filtering.${installFileInfo}")
                            return@setExcludeRspermFilter true
                        }

                        if (Utility.isExcludeEnginePkgName(installFileInfo.packageName)) {
                            RLog.v("isExcludeEnginePkgName filtering.${installFileInfo}")
                            return@setExcludeRspermFilter true
                        }

                        // 이미 설치된 apk 는 제외
                        if (installFileInfo.packageName.isInstalledPkg(get())) {
                            RLog.v("isInstalledPkg filtering.${installFileInfo}")
                            return@setExcludeRspermFilter true
                        }

                        RLog.v("installFileInfo.${installFileInfo}")
                        return@setExcludeRspermFilter false
                    }
                }
            }
        }


        single { Binder.getInstance() }
        single { EngineProviders.of(get()).get() }
        single<EventDispatcher.Factory> { EngineEventDispatcherFactory(get(), get()) }
        single { AgentStatus(PrefStatusContainer(get())) }
        single { KnoxManagerCompat() }
        factory<Updatable>(named("knox")) { KnoxKeyUpdate() }
        factory { RSPermService() }
        factory { PowerKeyController(get()) }
        factory { RunningAppFactory(get(), get(), get()) }

        factory { SystemInfo(get()) }


    }


    override fun getModules(): List<Module> {
        return listOf(
                interactorModule,
                utilModule,
                databaseModule,
                coreModule,
                viewModelModule
        )
    }
}