package com.rsupport.mobile.agent.utils

import android.graphics.Point
import android.graphics.PointF

/**
 * 기존 해상도와 변경된 해상도 사이의 좌표를 계산한다.
 * @param baseResolution 기존 해상도
 * @param targetResolution 변경된 해상도
 */
class ComputeCoordinate(
        private val baseResolution: Point = Point(),
        private val targetResolution: Point = Point()
) {
    companion object {
        const val ORIENTATION_PORTRAIT = 0
        const val ORIENTATION_LANDSCAPE = 1
    }

    private val computedPoint = Point()
    private val ratio: PointF = PointF().apply {
        x = targetResolution.x.toFloat() / baseResolution.x.toFloat()
        y = targetResolution.y.toFloat() / baseResolution.y.toFloat()
    }

    /**
     * baseResolution을 기준으로 입력된 포인트를 targetResolution 으로 변경된 좌표로 변경한다.
     * baseResolution을 설정하지 않으면 계산하지 않고 1:1(x, y 그대로) 로 반환한다.
     * targetResolution 설정하지 않으면 계산하지 않고 1:1(x, y 그대로) 로 반환한다.
     * @param x baseResolution 에 해당하는 x 좌표
     * @param y baseResolution 에 해당하는 y 좌표
     * @return targetResolution 에 해당하는 좌표로 변경된 좌표 Point
     */
    fun compute(x: Int, y: Int): Point {
        if (baseResolution.x == 0 || baseResolution.y == 0 || targetResolution.x == 0 || targetResolution.y == 0) {
            return computedPoint.apply {
                this.x = x
                this.y = y
            }
        }

        return computedPoint.apply {
            this.x = (ratio.x * x).toInt()
            this.y = (ratio.y * y).toInt()
        }
    }

    /**
     * tagetResolution 을 변경한다.
     * @param x targetResolution x
     * @param y targetResolution y
     */
    fun setTargetResolution(x: Int, y: Int) {
        targetResolution.apply {
            this.x = x
            this.y = y
        }
        updateRatio()
    }

    /**
     * baseResolution 을 변경한다.
     ** @param x targetResolution x
     * @param y targetResolution y
     */
    fun setBaseResolution(x: Int, y: Int) {
        baseResolution.apply {
            this.x = x
            this.y = y
        }
        updateRatio()
    }

    /**
     * baseResolution 과 targetResolution 을 sourceCoordinate의  값으로 설정한다.
     * @param sourceCoordinate
     */
    fun apply(sourceCoordinate: ComputeCoordinate) {
        setBaseResolution(sourceCoordinate.baseResolution.x, sourceCoordinate.baseResolution.y)
        setTargetResolution(sourceCoordinate.targetResolution.x, sourceCoordinate.targetResolution.y)
    }

    val orientation by lazy {
        if (baseResolution.x > baseResolution.y) ORIENTATION_LANDSCAPE
        else ORIENTATION_PORTRAIT
    }

    private fun updateRatio() {
        ratio.apply {
            this.x = targetResolution.x.toFloat() / baseResolution.x.toFloat()
            this.y = targetResolution.y.toFloat() / baseResolution.y.toFloat()
        }
    }
}