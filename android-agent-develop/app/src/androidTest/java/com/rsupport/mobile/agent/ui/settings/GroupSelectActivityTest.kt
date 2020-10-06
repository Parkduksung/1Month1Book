package com.rsupport.mobile.agent.ui.settings

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.ui.test.utils.LoginUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.hamcrest.core.StringContains
import org.junit.After
import org.junit.Before
import org.junit.Test

class GroupSelectActivityTest {
    private val loginUtils = LoginUtils()

    @Before
    fun setup() = runBlocking<Unit> {
        loginUtils.setup()
    }

    @After
    fun tearDown() = runBlocking<Unit> {
        loginUtils.tearDown()
    }

    // 루트에서 검색하여 검색 데이터가 있는지 확인한다.
    @Test
    fun groupSearchOnRootTest() = runBlocking<Unit> {
        loginUtils.launch()
        loginUtils.performLogin()

        // 그룹 선택
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(1)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())

        // 검색어 입력
        Espresso.onView(ViewMatchers.withId(R.id.search_edit))
                .perform(ViewActions.replaceText("a1"))

        // 검색 버튼
        Espresso.onView(ViewMatchers.withId(R.id.search_btn))
                .perform(ViewActions.click())

        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.group_list))
                .atPosition(0)
                .onChildView(ViewMatchers.withId(R.id.tvgroupname))
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString("a1"))))
    }

    // 한단계 자식 그룹에서 검색하여 검색 결과가 있는지 확인한다.
    @Test
    fun groupSearchOnDepth1Test() = runBlocking<Unit> {
        loginUtils.launch()
        loginUtils.performLogin()

        // 그룹 선택
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(1)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())

        // 그룹 선택 > child 1
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.group_list))
                .atPosition(0)
                .perform(ViewActions.click())

        // 검색어 입력
        Espresso.onView(ViewMatchers.withId(R.id.search_edit))
                .perform(ViewActions.replaceText("a1"))

        // 검색 버튼
        Espresso.onView(ViewMatchers.withId(R.id.search_btn))
                .perform(ViewActions.click())

        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.group_list))
                .atPosition(0)
                .onChildView(ViewMatchers.withId(R.id.tvgroupname))
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString("a1"))))
    }
}