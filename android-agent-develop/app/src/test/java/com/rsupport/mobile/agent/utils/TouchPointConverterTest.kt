package com.rsupport.mobile.agent.utils

import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class TouchPointConverterTest {

    @Mock
    lateinit var displaySize: DisplaySize

    @Mock
    lateinit var navigationBar: NavigationBar

    // 1. Top left 좌표기준을 Top right 기준으로 좌표를 변경한다.(x 좌표)
    @Test
    fun convertTL2TRXTest() = runBlocking {
        val x = 10
        Mockito.`when`(displaySize.getWidth()).thenReturn(200)
        Mockito.`when`(navigationBar.getNavigationDirection()).thenReturn(NavigationBar.LEFT)

        val converter = TouchPointConverter(displaySize, navigationBar)
        val trx = converter.convertXIfNavigationLeft(x)
        MatcherAssert.assertThat("좌표계산 값이 달라서 실패", trx, Matchers.`is`(190))
    }

    // 3. Direction 이 Right 일때 변경되지 않음을 테스트한다.
    @Test
    fun convertRightTest() = runBlocking {
        val x = 10
        Mockito.`when`(navigationBar.getNavigationDirection()).thenReturn(NavigationBar.RIGHT)

        val converter = TouchPointConverter(displaySize, navigationBar)
        val trx = converter.convertXIfNavigationLeft(x)
        MatcherAssert.assertThat("좌표계산 값이 달라서 실패", trx, Matchers.`is`(x))
    }

    // 3. Direction 이 Bottom 일때 변경되지 않음을 테스트한다.
    @Test
    fun convertBottomTest() = runBlocking {
        val x = 10
        Mockito.`when`(navigationBar.getNavigationDirection()).thenReturn(NavigationBar.BOTTOM)

        val converter = TouchPointConverter(displaySize, navigationBar)
        val trx = converter.convertXIfNavigationLeft(x)
        MatcherAssert.assertThat("좌표계산 값이 달라서 실패", trx, Matchers.`is`(x))
    }

}