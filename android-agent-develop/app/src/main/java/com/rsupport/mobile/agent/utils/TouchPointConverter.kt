package com.rsupport.mobile.agent.utils

/**
 * Viewer 에서 touch point 는 TOP|LEFT 기준으로 Agent 에 보내는데 Agent에서 NavigationBar 가 왼쪽에 있을경우에는
 * x 의 touch point 가 NavigationBar 이후좌표부터 x 좌표가 0으로되기때문에 TOP|RIGHT 기준으로 변경해서 좌표를 계산해야한다.
 */
class TouchPointConverter(private val displaySize: DisplaySize, private val navigationBar: NavigationBar) {
    fun convertXIfNavigationLeft(x: Int): Int {
        return if (navigationBar.getNavigationDirection() == NavigationBar.LEFT) {
            displaySize.getWidth() - x
        } else {
            x
        }
    }
}

interface DisplaySize {
    /**
     * Display 가로 크기를 반환한다.
     * @return 가로크기
     */
    fun getWidth(): Int

    /**
     * Display 세로 크기를 반환한다.
     * @return 세로크기
     */
    fun getHeight(): Int
}

interface NavigationBar {
    companion object {
        /**
         * NavigationBar 가 왼쪽에 위치해있음.
         */
        const val LEFT = 0

        /**
         * NavigationBar 가 오른쪽에 위치해잇음.
         */
        const val RIGHT = 1

        /**
         * NavigationBar 가 아래 위치해잇음.
         */
        const val BOTTOM = 2
    }

    /**
     * NavigationBar의 위치를 얻어온다.
     * @return navigationBar 의 위치
     * @see [NavigationBar.LEFT]
     * @see [NavigationBar.RIGHT]
     * @see [NavigationBar.BOTTOM]
     */
    fun getNavigationDirection(): Int
}