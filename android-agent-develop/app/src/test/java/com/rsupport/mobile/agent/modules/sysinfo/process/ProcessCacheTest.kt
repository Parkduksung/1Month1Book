package com.rsupport.mobile.agent.modules.sysinfo.process

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class ProcessCacheTest {

    private lateinit var processCache: ProcessCache
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        processCache = ProcessCache()
    }

    @After
    fun tearDown() {
        processCache.close()
    }

    @Test
    fun get_empty_items() {
        assertTrue(processCache.processItems.isEmpty())
    }

    @Test
    fun has_cached_item() {
        val pkgName = "test.pkg"
        putCache(pkgName)
        assertTrue(processCache.hasCached(pkgName))
    }

    @Test
    fun cached_item_when_put_cache() {
        putCache("test.pkg")

        assertNotNull(processCache.getCached("test.pkg"))
    }


    @Test
    fun get_when_added() {
        val pkgName = "test.pkg"
        addByPkg(pkgName)


        assertNotNull(processCache.findProcessItem(pkgName))
    }

    @Test
    fun null_when_clear() {
        putCache("test.pkg")

        processCache.clear()

        assertNull(processCache.findProcessItem("test.pkg"))
    }


    @Test
    fun new_list_when_generate() {
        putCache("test.pkg")

        val items = processCache.processItems
        val newItems = processCache.generateLocalList()

        assertEquals(items, newItems)
    }


    @Test
    fun order_by_name_when_ordered() {
        addByPkg("test2.pkg")
        addByPkg("test1.pkg")
        addByPkg("test0.pkg")

        processCache.orderByName()

        val processItems = processCache.processItems


        assert(processItems[0].runningAppInfo.pkgName.equals("test0.pkg"))
        assert(processItems[1].runningAppInfo.pkgName.equals("test1.pkg"))
        assert(processItems[2].runningAppInfo.pkgName.equals("test2.pkg"))
    }

    private fun addByPkg(pkgName: String) {
        processCache.add(
                ProcessItem(
                        context,
                        1024 * 104,
                        false,
                        RunningAppInfo(pkgName)
                )
        )
    }

    private fun putCache(pkgName: String) {
        processCache.putCache(
                pkgName,
                ProcessItem(
                        context,
                        1024 * 104,
                        false,
                        RunningAppInfo(pkgName)
                )
        )
    }


}