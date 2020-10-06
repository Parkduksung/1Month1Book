package base

import androidx.lifecycle.Observer
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import com.rsupport.mobile.agent.ui.base.ViewState

@RunWith(MockitoJUnitRunner::class)
abstract class ViewModelBaseTest : BaseTest() {

    @Mock
    lateinit var viewStateObserver: Observer<ViewState>
}