package com.example.a1month1book.kotlin200

import android.util.Log

object Part3 {


    // 동반자 객체, 동반객체는 어떤 클래스의 모든 인스턴스가 공유하는 객체를 만들고 싶을때 사용한다.

    // countCreated  숫자는 100 이다.
    fun companionObject() {


        for (i in 0 until 100) {
            Person.create()
        }

        Log.d("결과", Person.countCreated.toString())
    }


    // 가령 함수를 호출하게 되면 그 함수의 흐름으로 점프라고 그 함수가 끝나면 다시 원래도 점프해 돌아오는데,
    // 이러면서 미세하게 성능을 저하시키는데
    // fun 앞에 혹은 class 앞에 inline 을 붙이면 점프하는게 아닌 마치 하나의 공간에서 실행되게끔 해주는 것이 inline 함수이다.
    // 근데 막 좋다고 inline 쓰면은 오히려 프로그램 크기가 엄청 커지게 된다. inline 함수는 함수속 문장을 재활용하지 않기 때문이다.
    // 그래서 문장이 적고 빈번히 호출되는 함수만 inline 으로 만드는 것을 권장한다.
    fun inlineFunc() {

        noInline()
        noInline()
        noInline()

        inline()
        inline()
        inline()

        // 아래가 미세하게 빠름.

    }

    private fun noInline() {
        Log.d("결과", "1")
        Log.d("결과", "2")
    }

    private inline fun inline() {
        Log.d("결과", "1")
        Log.d("결과", "2")
    }

    // const 는 변수에 접근하는 코드를 변수에 저장된 값으로 대체시킨다.


    // 동반자 객체의 확장함수.

    class Student {
        companion object
    }

    // <-----------------> 여기까지 확장함수 같은 건데 Companion 이 붙은거임.
    // 근데 이걸 쓸까..?
    fun Student.Companion.create() = Student()

    fun extensionFunctionCompanionObject() {
        Student.create()
    }

    fun diamondProblem() {


    }

    interface Parent {
        fun follow(): Int
    }

    interface Mother : Parent {
        override fun follow(): Int {
            return Log.d("결과", "follow Mother!")
        }
    }

    interface Father : Parent {
        override fun follow(): Int {
            return Log.d("결과", "follow Father!")
        }
    }

    //    // 이런식으로 하게되면 오류난다.
//    class Child : Mother, Father{
//        override fun follow(): Int {
//            return super.follow()
//        }
//    }

    // 위의 다이아몬드 모양의 구조를 해결할 수 있다.
    class Child : Mother, Father {
        override fun follow(): Int {
            return super<Mother>.follow()
        }
    }

    // data 클래스
    // 적어도 하나의 프로퍼티를 가져야한다.
    // 생성자 매개변수에는 반드시 var / val 을 같이 써야 한다. 즉, 프로퍼티에 대응하지 않는 생성자 매게변수를 가질 수 없다.
    // abstract, open, sealed, inner 키워드를 붙일 수 없다.
    // 인터페이스만 구현 가능하되 sealed 클래스도 상속 가능하다.
    // 맴버 함수를 선언할 수 없다.

    // 좋은건 Any 클래스에 들어있는 equals, hashCode, toString 멤버함수가 자동으로 오버라이딩한다.


    // 객체 분해하기..
    fun destructuringObject() {

        val (name, _, salary) = Employee("John", 30, 3300)

        Log.d("결과", name)
        Log.d("결과", salary.toString())

    }
    data class Employee(val name: String, val age: Int, val salary: Int)



}


class Person private constructor() {

    companion object {

        fun create(): Person {
            countCreated += 1
            return Person()
        }

        var countCreated = 0
            private set
    }

}