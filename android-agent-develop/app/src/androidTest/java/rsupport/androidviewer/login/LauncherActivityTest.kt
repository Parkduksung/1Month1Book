package rsupport.androidviewer.login

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.launcher.LauncherActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class LauncherActivityTest {

    @Before
    fun setup() = runBlocking<Unit> {
        val agentInfoInteractor: AgentInfoInteractor by inject(AgentInfoInteractor::class.java)
        agentInfoInteractor.removeAgent()
        agentInfoInteractor.release()

        ActivityScenario.launch(LauncherActivity::class.java)
    }

    // 튜토리얼을 보지 않았을때 튜토리얼이 노출되는지 확인한다.
    @Test
    fun tutorialShowTest() = runBlocking<Unit> {
        onView(withId(R.id.introgallery)).check(matches(ViewMatchers.isDisplayed()))
        delay(1000)
    }
}