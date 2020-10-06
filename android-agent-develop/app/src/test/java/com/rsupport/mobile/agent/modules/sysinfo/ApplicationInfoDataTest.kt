package com.rsupport.mobile.agent.modules.sysinfo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningApplication
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoCache
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoHolder
import org.hamcrest.core.StringContains.containsString
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
class ApplicationInfoDataTest {
    private lateinit var applicationInfo: ApplicationInfo

    private val context: Context = spy(ApplicationProvider.getApplicationContext<Context>())
    private val packageManager = mock(PackageManager::class.java)
    private val runningAppFactory = mock(RunningAppFactory::class.java)
    private val runningApplication = mock(RunningApplication::class.java)
    private val runningListOneApp = listOf(RunningAppInfo("test.pkg"))
    private val runningListSomeApps = runningListOneApp + listOf(RunningAppInfo("test1.pkg"))

    @Before
    fun setup() {
        whenever(context.packageManager).thenReturn(packageManager)
        loadKoinModules(module(override = true) { factory { runningAppFactory } })
        whenever(runningAppFactory.create()).thenReturn(runningApplication)
    }

    @After
    fun tearDown() {
        stopKoin()
        applicationInfo.close()
    }

    @Test
    fun returnTrueWhenRunningAppIsZero() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            runningAppInfos = emptyList()
        }.build()
        val isRunning = applicationInfo.isAppRunning("test")
        assertTrue(isRunning)
    }

    @Test
    fun returnTrueWhenRunningAppIsOne() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            runningAppInfos = runningListOneApp
        }.build()

        val isRunning = applicationInfo.isAppRunning("test")
        assertTrue(isRunning)
    }

    @Test
    fun returnTrueWhenRunningAppIsMoreThanOne() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            runningAppInfos = runningListSomeApps
            label = "test"
        }.build()
        val isRunning = applicationInfo.isAppRunning("test")
        assertTrue(isRunning)
    }

    @Test
    fun returnFalseWhenRunningAppIsMoreThanOne() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            runningAppInfos = runningListSomeApps
            label = "test"
        }.build()
        val isRunning = applicationInfo.isAppRunning("not_found_pkg")
        assertFalse(isRunning)
    }

    @Test
    fun returnNullWhenCalledTheGetPackageUriFromInTargetNameNull() {
        applicationInfo = GivenApplicationInfoBuilder().build()
        val resultUri = applicationInfo.getPackageUriFrom(null)
        assertNull(resultUri)
    }

    @Test
    fun returnNullWhenCalledTheGetPackageUriFromIfAppInfoHolderIsEmpty() {
        applicationInfo = GivenApplicationInfoBuilder().build()
        val resultUri = applicationInfo.getPackageUriFrom("test")
        assertNull(resultUri)
    }

    @Test
    fun returnUriWhenCalledTheGetPackageUriFromIfTheParamsNameIsEqualsALabelName() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "test"
            packageName = "test.pkg"
        }.build()

        val resultUri = applicationInfo.getPackageUriFrom("test")
        assertNotNull(resultUri)
    }

    @Test
    fun returnUriWhenCalledTheGetPackageUriFromIfTheParamsNameIsEqualsPkgName() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "test"
            packageName = "test.pkg"
        }.build()
        val resultUri = applicationInfo.getPackageUriFrom("test.pkg")
        assertNotNull(resultUri)
    }

    @Test
    fun returnUriWhenCalledTheGetPackageUriFromIfHolderLabelIsNull() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = null
            packageName = "test.pkg"
        }.build()

        val resultUri = applicationInfo.getPackageUriFrom("test.pkg")
        assertNotNull(resultUri)
    }


    @Test
    fun returnNullWhenCalledTheGetAppDetailInfoPacketIfNotFoundDetailInfo() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            nameNotFoundExceptionWhenGetApplicationInfo = PackageManager.NameNotFoundException("not found name")
        }.build()

        val detailResult = applicationInfo.getAppDetailInfoPacket("test")
        assertNull(detailResult)
    }

    @Test
    fun returnNullWhenCalledTheGetAppDetailInfoPacketIfAppItemIsNull() {
        applicationInfo = GivenApplicationInfoBuilder().build()
        val detailResult = applicationInfo.getAppDetailInfoPacket(null)
        assertNull(detailResult)
    }

    @Test
    fun checkDetailDataWhenCalledGetAppDetailInfoPacketIfGetAppInfo() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            className = "test.class"
            dataDir = "/data/test/dir"
            appName = "app.name"
            packageName = "test.pkg"
            permission = "granted"
            sourceDir = "/data/test/source"
            targetSdkVersion = 21
            uid = 123
        }.build()

        val detailResult = applicationInfo.getAppDetailInfoPacket("test")
        val resultString = String(detailResult, 4, detailResult.size - 6, StandardCharsets.UTF_16LE).trim()

        assertApplicationDetailInfo(resultString)
    }

    @Test
    fun resultFalseWhenCalledTheRunAppIfEmptyCache() {
        applicationInfo = GivenApplicationInfoBuilder().build()

        val runResult = applicationInfo.runApp("test")
        assertFalse(runResult)
    }

    @Test
    fun resultFalseWhenCalledTheRunAppIfNullLabelName() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            packageName = "test.pkg"
        }.build()

        val runResult = applicationInfo.runApp("not_found_label")
        assertFalse(runResult)
    }

    @Test
    fun resultFalseWhenCalledTheRunAppIfNotFoundLabelName() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "test"
            packageName = "test.pkg"
        }.build()

        val runResult = applicationInfo.runApp("not_found_label")
        assertFalse(runResult)
    }

    @Test
    fun resultFalseWhenCalledTheRunAppIfLauncherIntentNotFound() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "test"
            packageName = "test.pkg"
        }.build()

        val runResult = applicationInfo.runApp("test")
        assertFalse(runResult)
    }

    @Test
    fun resultTrueWhenCalledTheRunAppIfPkeIsBrowser() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "browser"
            packageName = "browser.pkg"
            launchIntent = Intent()
        }.build()

        val runResult = applicationInfo.runApp("browser")
        assertTrue(runResult)
    }

    @Test
    fun resultFalseWhenCalledTheRunAppIfStartActivity() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "test"
            packageName = "test.pkg"
            launchIntent = Intent()
            activityNotFoundException = ActivityNotFoundException("not found activity")
        }.build()

        val runResult = applicationInfo.runApp("test")
        assertFalse(runResult)
    }

    @Test
    fun resultTrueWhenCalledTheRunAppIfStartActivity() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "test"
            packageName = "test.pkg"
            launchIntent = Intent()
        }.build()
        val runResult = applicationInfo.runApp("test")
        assertTrue(runResult)
    }

    @Test
    fun resultFalseWhenCalledTheRunAppIfStartContactActivity() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "contact"
            packageName = "contact.pkg"
            launchIntent = Intent()
            activityNotFoundException = ActivityNotFoundException("not found activity")
        }.build()

        val runResult = applicationInfo.runApp("contact")
        assertFalse(runResult)
    }

    @Test
    fun resultTrueWhenCalledTheRunAppIfStartContactActivity() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            label = "contact"
            packageName = "contact.pkg"
            launchIntent = Intent()
            activityNotFoundException = ActivityNotFoundException("not found activity")
            startContactActivity = true
        }.build()
        val runResult = applicationInfo.runApp("contact")
        assertTrue(runResult)
    }


    @Test
    fun returnEmptyDataWhenLoadAppsIfAppInfosIsEmpty() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            isWifiConnected = true
        }.build()

        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)
        assertEquals(0, appData.size)
    }

    @Test
    fun returnOneDataWhenLoadAppsAllIfAppInfoHasOne() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            packageName = "test.pkg"
            isWifiConnected = true
        }.build()

        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)
        assertEquals(1, appData.size)
    }

    @Test
    fun returnSomeDatasWhenLoadAppsAllIfAppInfoHasSome() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            installedPackages = listOf(
                    android.content.pm.ApplicationInfo().apply {
                        packageName = "test1.pkg"
                    },
                    android.content.pm.ApplicationInfo().apply {
                        packageName = "test2.pkg"
                    },
                    android.content.pm.ApplicationInfo().apply {
                        packageName = "test3.pkg"
                    }
            )
        }.build()

        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)
        assertEquals(3, appData.size)
    }

    @Test
    fun checkDataWhenLoadAppsAllIfAppInfoHasOne() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            installedPackages = listOf(
                    android.content.pm.ApplicationInfo().apply {
                        sourceDir = "/data/app-private"
                        packageName = "test.pkg"
                    }
            )
            versionName = "1.0.0"
            versionCode = 10
        }.build()


        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)

        val appDataReader = AppDataReader(appData)

        assertEquals(1, appDataReader.count)
        assertEquals(0.toByte(), appDataReader.type)
        assertThat(appDataReader.text, containsString("test.pkg&/"))
        assertThat(appDataReader.text, containsString("1.0.0&/"))
        assertThat(appDataReader.text, containsString("OFF&/"))
        assertThat(appDataReader.text, containsString("1&/"))
    }

    @Test
    fun checkAppIconDataWhenLoadAppsAllIfAppInfoHasOne() {
        val compressedIconSize = getCompressedIconBitmapByteCount()
        applicationInfo = GivenApplicationInfoBuilder().apply {
            packageName = "test.pkg"
            versionName = "1.0.0"
            isWifiConnected = true
            icon = BitmapDrawable(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        }.build()

        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)

        val appDataReader = AppDataReader(appData)

        assertEquals(1, appDataReader.count)
        assertEquals(compressedIconSize, appDataReader.bitmapLength)
        assertEquals(appDataReader.text, "test.pkg&/1.0.0&/OFF&/1&/1")
    }

    @Test
    fun checkAppRunningStateWhenLoadApps() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            packageName = "test.pkg"
            versionName = "1.0.0"
            isWifiConnected = true
            runningAppInfos = listOf(RunningAppInfo("test.pkg"))
        }.build()


        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)

        val appDataReader = AppDataReader(appData)

        assertEquals(1, appDataReader.count)
        assertEquals(appDataReader.text, "test.pkg&/1.0.0&/ON&/1&/1")
    }

    @Test
    fun checkRemovableFalseStateWhenLoadApps() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            packageName = "test.pkg"
            versionName = "1.0.0"
            isWifiConnected = true
            removable = false
            runningAppInfos = listOf(RunningAppInfo("test.pkg"))
        }.build()


        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_ALL)

        val appDataReader = AppDataReader(appData)
        assertEquals(1, appDataReader.count)
        assertEquals(appDataReader.text, "test.pkg&/1.0.0&/ON&/0&/1")
    }

    @Test
    fun returnSystemAppWhenLoadAppsIfFilterSystemAPP() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            installedPackages = listOf(
                    android.content.pm.ApplicationInfo().apply {
                        flags = android.content.pm.ApplicationInfo.FLAG_SYSTEM
                        packageName = "test.pkg"
                        label = "test"
                    },
                    android.content.pm.ApplicationInfo().apply {
                        flags = android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
                        packageName = "test1.pkg"
                        label = "test1"
                    }
            )
        }.build()

        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_SYS)
        assertEquals(1, appData.size)
    }

    @Test
    fun returnUserAppWhenLoadAppsIfFilterUserAPP() {
        applicationInfo = GivenApplicationInfoBuilder().apply {
            installedPackages = listOf(
                    android.content.pm.ApplicationInfo().apply {
                        flags = android.content.pm.ApplicationInfo.FLAG_SYSTEM
                        packageName = "test.pkg"
                        label = "test"
                    },
                    android.content.pm.ApplicationInfo().apply {
                        flags = android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
                        packageName = "test1.pkg"
                        label = "test1"
                    }
            )
        }.build()

        val appData = applicationInfo.loadApps(ApplicationInfo.APP_TYPE_USER)
        assertEquals(1, appData.size)
    }

    private fun getCompressedIconBitmapByteCount(): Int {
        val iconBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val oStream = ByteArrayOutputStream()
        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream)

        val compressedIconSize = oStream.size()
        oStream.close()
        return compressedIconSize
    }

    private fun assertApplicationDetailInfo(resultString: String) {
        assertThat(resultString, containsString("ClassName&/test.class&&"))
        assertThat(resultString, containsString("DataDir&//data/test/dir&&"))
        assertThat(resultString, containsString("Name&/app.name&&"))
        assertThat(resultString, containsString("PackageName&/test.pkg&&"))
        assertThat(resultString, containsString("Permission&/granted&&"))
        assertThat(resultString, containsString("SourceDir&//data/test/source&&"))
        assertThat(resultString, containsString("TargetSDKVersion&/21&&"))
        assertThat(resultString, containsString("UID&/123&&"))
    }

    class AppDataReader(appBuffer: Array<ByteArray>) {

        init {
            read(appBuffer)
        }

        var type: Byte = 0
            private set

        var textLength: Int = 0
            private set

        var bitmapLength: Int = 0
            private set

        lateinit var text: String
            private set

        var count: Int = 0
            private set

        private fun getByteBuffer(appData: Array<ByteArray>): ByteBuffer {
            val dataBuffer = ByteBuffer.allocate(appData[0].size)
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN)
            dataBuffer.put(appData[0])
            dataBuffer.flip()
            return dataBuffer
        }

        private fun getAppInfoString(appData: Array<ByteArray>, textLength: Int) = String(appData[0], 9, textLength - 2, StandardCharsets.UTF_16LE)

        private fun read(appBuffer: Array<ByteArray>) {
            val dataBuffer = getByteBuffer(appBuffer)
            count = appBuffer.size
            type = dataBuffer.get()
            textLength = dataBuffer.int
            bitmapLength = dataBuffer.int
            text = getAppInfoString(appBuffer, textLength)
        }

    }

    inner class GivenApplicationInfoBuilder {
        var applicationInfoHelper: ApplicationInfoHelper? = null
        var removable = true
        var packageName: String? = null
        var sourceDir = "/data/app-private"
        var versionName = "1.0.0"
        var versionCode: Int? = null
        var icon: BitmapDrawable? = null
        var label: String? = null
        var isWifiConnected: Boolean? = null
        var runningAppInfos: List<RunningAppInfo> = emptyList()
        var appInfoCache: AppInfoCache? = null
        var nameNotFoundExceptionWhenGetApplicationInfo: PackageManager.NameNotFoundException? = null
        var activityNotFoundException: ActivityNotFoundException? = null

        var className: String? = null
        var dataDir: String? = null
        var appName: String? = null
        var permission: String? = null
        var targetSdkVersion: Int? = null
        var uid: Int? = null
        var launchIntent: Intent? = null
        var startContactActivity: Boolean? = null
        var installedPackages: List<android.content.pm.ApplicationInfo>? = null

        private fun createMockApplicationHelperIfNull(): ApplicationInfoHelper {
            if (applicationInfoHelper == null) {
                applicationInfoHelper = mock(ApplicationInfoHelper::class.java)
            }
            return applicationInfoHelper!!
        }

        fun build(): ApplicationInfo {
            label?.let {
                applicationInfoHelper = mock(ApplicationInfoHelper::class.java)
                whenever(applicationInfoHelper?.getLabelName(any())).thenReturn(it)
            }

            className?.let {
                createMockApplicationHelperIfNull().let {
                    whenever(it.getApplicationInfo(any())).thenReturn(
                            android.content.pm.ApplicationInfo().apply {
                                packageName = this@GivenApplicationInfoBuilder.packageName
                                className = this@GivenApplicationInfoBuilder.className
                                dataDir = this@GivenApplicationInfoBuilder.dataDir
                                name = this@GivenApplicationInfoBuilder.appName
                                packageName = this@GivenApplicationInfoBuilder.packageName
                                permission = this@GivenApplicationInfoBuilder.permission
                                sourceDir = this@GivenApplicationInfoBuilder.sourceDir
                                targetSdkVersion = this@GivenApplicationInfoBuilder.targetSdkVersion
                                        ?: 0
                                uid = this@GivenApplicationInfoBuilder.uid ?: 0
                                flags = android.content.pm.ApplicationInfo.FLAG_SYSTEM
                                processName = this@GivenApplicationInfoBuilder.packageName
                            }
                    )
                }
            }

            nameNotFoundExceptionWhenGetApplicationInfo?.let {
                createMockApplicationHelperIfNull().let {
                    whenever(it.getApplicationInfo(any())).thenThrow(nameNotFoundExceptionWhenGetApplicationInfo)
                }
            }

            activityNotFoundException?.let {
                createMockApplicationHelperIfNull().let {
                    whenever(it.startActivity(any())).thenThrow(activityNotFoundException)
                }
            }

            startContactActivity?.let { isStartContactActivity ->
                createMockApplicationHelperIfNull().let {
                    whenever(it.startContactActivity(any())).thenReturn(isStartContactActivity)
                }
            }

            launchIntent?.let {
                createMockApplicationHelperIfNull().let {
                    whenever(it.getLaunchIntent(any())).thenReturn(Intent())
                }
            }

            isWifiConnected?.let { isWifiConnected ->
                createMockApplicationHelperIfNull().let {
                    whenever(it.isWifiConnected()).thenReturn(isWifiConnected)
                }
            }

            installedPackages?.let { installedAppList ->
                createMockApplicationHelperIfNull().let {
                    whenever(it.getInstalledApplications()).thenReturn(installedAppList)
                }
            }

            whenever(runningApplication.getRunningAppInfos()).thenReturn(runningAppInfos)

            packageName?.let {
                appInfoCache = mock(AppInfoCache::class.java)
                whenever(appInfoCache?.appInfoHolders).thenReturn(arrayListOf(
                        AppInfoHolder().apply {
                            icon = this@GivenApplicationInfoBuilder.icon
                            label = this@GivenApplicationInfoBuilder.label
                            version = this@GivenApplicationInfoBuilder.versionName
                            versionCode = this@GivenApplicationInfoBuilder.versionCode ?: 0
                            removable = this@GivenApplicationInfoBuilder.removable

                            appInfo = android.content.pm.ApplicationInfo().apply {
                                packageName = this@GivenApplicationInfoBuilder.packageName
                                className = this@GivenApplicationInfoBuilder.className
                                dataDir = this@GivenApplicationInfoBuilder.dataDir
                                name = this@GivenApplicationInfoBuilder.appName
                                packageName = this@GivenApplicationInfoBuilder.packageName
                                permission = this@GivenApplicationInfoBuilder.permission
                                sourceDir = this@GivenApplicationInfoBuilder.sourceDir
                                targetSdkVersion = this@GivenApplicationInfoBuilder.targetSdkVersion
                                        ?: 0
                                uid = this@GivenApplicationInfoBuilder.uid ?: 0
                                flags = android.content.pm.ApplicationInfo.FLAG_SYSTEM
                                processName = this@GivenApplicationInfoBuilder.packageName
                            }
                        }
                ))
            }

            versionCode?.let { versionCode ->
                createMockApplicationHelperIfNull().let {
                    whenever(it.getPackageInfo(any())).thenReturn(
                            PackageInfo().apply {
                                this.versionName = this@GivenApplicationInfoBuilder.versionName
                                this.versionCode = versionCode
                            }
                    )
                }
            }

            return ApplicationInfo(
                    context,
                    applicationInfoHelper ?: ApplicationInfoHelper(context),
                    appInfoCache ?: AppInfoCache())
        }
    }
}
