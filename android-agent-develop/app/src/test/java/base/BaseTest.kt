package base

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.koin.KoinBaseSetup
import com.rsupport.util.log.RLog
import com.rsupport.util.log.printer.ILogPrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(MockitoJUnitRunner::class)
abstract class BaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var application: Application

    @Mock
    lateinit var context: Context

    protected abstract fun createModules(): List<Module>

    private val koinSetup = object : KoinBaseSetup() {
        override fun getModules(): List<Module> {
            return createModules()
        }
    }

    @Before
    open fun setup() {
        RLog.setLogPrinter(object : ILogPrinter {

            override fun print(tag: String, p1: RLog.Level?, message: String) {
                println("$tag $message")
            }

            override fun getName(): String = ""

            override fun setMinimumPrintLevel(p0: RLog.Level?) {}

        })

        Dispatchers.setMain(TestCoroutineDispatcher())
        Mockito.`when`(context.applicationContext).thenReturn(context)
        koinSetup.setup(context)
    }

    @After
    open fun tearDown() {
        stopKoin()
    }
}

inline fun <reified T> mock() = Mockito.mock(T::class.java)