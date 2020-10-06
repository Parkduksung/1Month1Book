package com.rsupport.mobile.agent.utils

import android.graphics.Point
import com.rsupport.mobile.agent.TestApplication
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class ComputeCoordinateTest {

    // 1. 기준 해상도 좌표를 같은 해상도 좌표로 계산한다.
    @Test
    fun computeCoordinateSameResolution() = runBlocking {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(100, 200)
        )
        val x = 50
        val y = 60

        val computedPoint = computeCoordinate.compute(x, y)

        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }

    // 2. 기준 해상도 좌표를 2배 큰 해상도 좌표로 계산한다.
    @Test
    fun computeCoordinate2_0xResolution() = runBlocking {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(200, 400)
        )
        val x = 50
        val y = 60

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x * 2))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y * 2))
    }


    // 3. 기준 해상도 좌표를 1.5배 큰 해상도 좌표로 계산한다.
    @Test
    fun computeCoordinate1_5xResolution() = runBlocking {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(150, 300)
        )
        val x = 50
        val y = 60

        val computedPoint = computeCoordinate.compute(x, y)

        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`((x * 1.5).toInt()))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`((y * 1.5).toInt()))
    }


    // 4. 기준 해상도 좌표를 0.5배 작은 해상도 좌표로 계산한다.
    @Test
    fun computeCoordinate0_5xResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(50, 100)
        )
        val x = 50
        val y = 60

        val computedPoint = computeCoordinate.compute(x, y)

        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`((x * 0.5).toInt()))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`((y * 0.5).toInt()))
    }


    // 5. 기준 해상도 좌표를 0.25배 작은 해상도 좌표로 계산한다.
    @Test
    fun computeCoordinate0_25xResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(25, 50)
        )
        val x = 50
        val y = 60

        val computedPoint = computeCoordinate.compute(x, y)

        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`((x * 0.25).toInt()))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`((y * 0.25).toInt()))
    }

    // 6. targetResolution 을 2배로 변경한후 테스트한다.
    @Test
    fun computeCoordinateAfter2_0xResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(100, 100)
        )
        val x = 50
        val y = 60

        computeCoordinate.setTargetResolution(200, 400)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x * 2))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y * 2))
    }


    // 7. targetResolution 을 1.5배로 변경한후 테스트한다.
    @Test
    fun computeCoordinateAfter1_5xResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(100, 100)
        )
        val x = 50
        val y = 60

        computeCoordinate.setTargetResolution(150, 300)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`((x * 1.5).toInt()))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`((y * 1.5).toInt()))
    }

    // 8. targetResolution 을 0.5배로 변경한후 테스트한다.
    @Test
    fun computeCoordinateAfter0_5xResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(100, 100)
        )
        val x = 50
        val y = 60

        computeCoordinate.setTargetResolution(50, 100)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`((x * 0.5).toInt()))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`((y * 0.5).toInt()))
    }


    // 9. targetResolution 을 0.5배로 변경한후 테스트한다.
    @Test
    fun computeCoordinateAfter0_25xResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(100, 100)
        )
        val x = 50
        val y = 60

        computeCoordinate.setTargetResolution(25, 50)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`((x * 0.25).toInt()))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`((y * 0.25).toInt()))
    }

    // 10. baseResolution 을 2배로 변경후 2배로 테스트한다.
    @Test
    fun computeCoordinateAfter2_0xBaseResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(200, 400)
        )
        val x = 50
        val y = 60

        computeCoordinate.setBaseResolution(200, 400)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }

    // 11. baseResolution 을 1.5배로 변경후 1.5배로 테스트한다.
    @Test
    fun computeCoordinateAfter1_5xBaseResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(150, 300)
        )
        val x = 50
        val y = 60

        computeCoordinate.setBaseResolution(150, 300)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }

    // 12. baseResolution 을 0.5배로 변경후 0.5배로 테스트한다.
    @Test
    fun computeCoordinateAfter0_5xBaseResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(50, 100)
        )
        val x = 50
        val y = 60

        computeCoordinate.setBaseResolution(50, 100)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }

    // 13. baseResolution 을 0.25배로 변경후 0.25배로 테스트한다.
    @Test
    fun computeCoordinateAfter0_25xBaseResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate(
                Point(100, 200), Point(25, 50)
        )
        val x = 50
        val y = 60

        computeCoordinate.setBaseResolution(25, 50)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }

    // 14. baseResolution 을 설정하지 않고 계산하면 1:1 로 반환한다.
    @Test
    fun computeCoordinateNoSetBaseResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate()
        val x = 50
        val y = 60

        computeCoordinate.setTargetResolution(100, 500)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }


    // 15. targetResolution 을 설정하지 않고 계산하면 1:1 로 반환한다.
    @Test
    fun computeCoordinateNoSetTargetResolution() = runBlocking<Unit> {
        val computeCoordinate = ComputeCoordinate()
        val x = 50
        val y = 60

        computeCoordinate.setBaseResolution(100, 500)

        val computedPoint = computeCoordinate.compute(x, y)


        MatcherAssert.assertThat("X 값이 달라서 실패", computedPoint.x, Matchers.`is`(x))
        MatcherAssert.assertThat("Y 값이 달라서 실패", computedPoint.y, Matchers.`is`(y))
    }

    // 16. 다른 객체의 base, target Resolution 의 값으로 설정후 확인한다.
    @Test
    fun computeCoordinateApplyTest() = runBlocking<Unit> {
        val baseCoordinate = ComputeCoordinate()
        val targetCoordinate = ComputeCoordinate(Point(100, 200), Point(200, 400))

        baseCoordinate.apply(targetCoordinate)

        val baseResult = baseCoordinate.compute(10, 20)
        val targetResult = targetCoordinate.compute(10, 20)

        MatcherAssert.assertThat("x 값이 달라서 실패", baseResult.x, Matchers.`is`(targetResult.x))
        MatcherAssert.assertThat("y 값이 달라서 실패", baseResult.y, Matchers.`is`(targetResult.y))
    }

    // 17. 가로 상태를 확인한다.
    @Test
    fun landscapeTest() = runBlocking<Unit> {
        val baseCoordinate = ComputeCoordinate()
        baseCoordinate.setBaseResolution(100, 30)

        MatcherAssert.assertThat("가로 상태가 아니라서 실패", baseCoordinate.orientation, Matchers.`is`(ComputeCoordinate.ORIENTATION_LANDSCAPE))
    }

    // 18. 세로 상태를 확인한다.
    @Test
    fun portraitTest() = runBlocking<Unit> {
        val baseCoordinate = ComputeCoordinate()
        baseCoordinate.setBaseResolution(30, 100)

        MatcherAssert.assertThat("세로 상태가 아니라서 실패", baseCoordinate.orientation, Matchers.`is`(ComputeCoordinate.ORIENTATION_PORTRAIT))
    }


    // 19. 가로,세로 가 같으면 세로 상태임을 확인한다.
    @Test
    fun sameResolutionPortraitTest() = runBlocking<Unit> {
        val baseCoordinate = ComputeCoordinate()
        baseCoordinate.setBaseResolution(100, 100)

        MatcherAssert.assertThat("세로 상태가 아니라서 실패", baseCoordinate.orientation, Matchers.`is`(ComputeCoordinate.ORIENTATION_PORTRAIT))
    }

}