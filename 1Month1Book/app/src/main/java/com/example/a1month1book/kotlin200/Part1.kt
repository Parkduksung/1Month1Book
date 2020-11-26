package com.example.a1month1book.kotlin200

import android.util.Log

object Part1 {


    // while 문 2개로 연립방정식을 만든다고 할때
    // break 걸때 가장 가까운 반복문 1개만 빠져나오게 되는데
    // 이런 문제를 해결하기 위해 코틀린에서는 Label 이라는 문법을 제공한다.
    fun label() {

        //label x

        var x = 0
        var y = 0

        while (x <= 20) {
            y = 0
            while (y <= 20) {
                if (x + y == 15 && x - y == 5)
                    break  //처음 이때 되는 순간의 해를 구하고 싶은데 그렇지 않고 밖에 while 문이 돌아서 loop 가 다시돌게된다.
                y += 1
            }
            x += 1
        }

        // x : 21 , y : 21
        Log.d("결과", "x : $x , y : $y")

        //label x

        var labelX = 0
        var labelY = 0

        outer@ while (labelX <= 20) {
            labelY = 0
            while (labelY <= 20) {
                if (labelX + labelY == 15 && labelX - labelY == 5)
                    break@outer  //처음 이때 되는 순간의 해를 구하고 싶은데 그렇지 않고 밖에 while 문이 돌아서 loop 가 다시돌게된다.
                labelY += 1
            }
            labelX += 1
        }

        // x : 10 , y : 5
        Log.d("결과", "x : $labelX , y : $labelY")


    }

    // n개의 인수를 받는 함수를 만들고 싶으면 어떻게 하냐?
    // 답은 vararg 키워드 를 붙인다.
    fun `겨변인수`(vararg num : Int){
//        이런식으로 n 개의 Int 형을 매개변수로 받을 수 있다.
    }

    //tip 이름 중복 관련하여
    // 지역변수랑 전역변수랑 이름이 같을때에는 main 가 가까운 함수의 a 로 인식.

    //40번 다시한번 보기 (메모리의 스택 영역.)

}