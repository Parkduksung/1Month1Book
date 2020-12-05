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


    //여기서부터는 조금 중요하다 생각하는 부분임..

    // 일단 해석이 중요함.
    // (Int) -> Unit  : 매개변수가 int 타입이고 반환타입이 Unit 인 함수를 저장할 수 있는 타입! 이런타입을 함수타입이라 함.
    //

    // 상수 vs 리터럴
    //상수는 변하지 않는 변수를 의미하며(메모리 위치) 메모리 값을 변경할 수 없다.
    //리터럴은 변수의 값이 변하지 않는 데이터(메모리 위치안의 값)를 의미한다.
    fun literalAndLambda() {

        val instantFunc: (Int?) -> Unit
        instantFunc =
            { number: Int? ->         // 지금 이부분이 함수 리터럴이다. 말그대로 함수를 나타내는 리터럴. 리터럴이란말은 문자그래로의 라고 해석되는데 그냥 문자? 라고 생각해도 될련지..
                Log.d(
                    "결과",
                    "$number"
                ) // 리터럴이란 데이터라고 생각하면 이해가 쉽겠고 val int = 1 일때 1이 리터럴이고 이는 변하지 않는 데이터값을 리터럴이라고 한다.
            } // 해석 : instantFunc 참조 변수에 (Int) -> Unit 타입의 함수가 저장된다. 즉 참조변수가 가리키는 함수를 호출하고 있다. 함수타입은 참조타입이므로 위치를 가리키는 형태로 저장됨 스택x
        // 함수를 담고 있는 변수는 마치 함수인 것처럼 호출할 수 있다.
        // 함수 리터럴에는 return 을 적지 않는다. 함수 리터럴의 반환 값은 함수 내용의 맨 마지막 표현식이 된다.


        // 일반적으로 () 로 바로 호출하면 되지만 변수가 Nullable 일때는 invoke 를 통해서 Null 처리를 하면 된다.

        instantFunc(33)
        instantFunc.invoke(33)

        // { 매개변수 -> 반환 값 } 형태를 람다식이라고 한다.


    }

    //익명함수.
    // 위와 비슷함. 단 return으로 반환값을 직접 지정해 줄 수 있다.
    fun anonymousFunction() {

        val instantFunc: (Int) -> Unit = {
            Log.d("결과", it.toString())
        }

        val instantFunc1: (Int) -> Unit = fun(number: Int): Unit {
            Log.d("결과", number.toString())
        }

    }

    //함수참조.
    // 함수 타입의 변수는 이미 선언되어 있는 함수나 객체의 멤버 함수를 가리킬 수도 있다.
    // 함수 이름 앞에 :: 를 붙이면 표현식의 값은 그 함수의 참조값이 되며, 타입은 그 함수의 시그니처에 맞는 함수 타입이 된다.
    fun functionReference() {
        var instantFunc: (Int, Int) -> Int
        instantFunc = ::plus
        instantFunc(60, 27)

        instantFunc = Object::minus
        instantFunc(36, 13)

        instantFunc = Class()::average
        instantFunc(25, 15)
    }

    fun plus(a: Int, b: Int) = Log.d("결과", "${a + b}")

    object Object {
        fun minus(a: Int, b: Int) = Log.d("결과", "${a - b}")
    }

    class Class {
        fun average(a: Int, b: Int) = Log.d("결과", "${(a + b) / 2}")
    }


    // 클로저
    // 선언될 당시의 상황을 기억하는 함수인 클로저.
    // ex) () -> Unit

    fun closure() {

        val f: () -> Unit = returnFunc(30)
        f()
    }

    private fun returnFunc(i: Int): () -> Unit = {
        println(i)
    }
    // f 호출 시점 -> i 매개변수가 이미 사라지고 없다. => returnFunc 함수가 끝나는 순간 num 매개변수는 소멸하기 때문 => 근데 어떻게 가능함?
    //=> 함수 리터럴이 자신이 만들어질때의 상황을 기억하고 있음. 205~206 line 에서 함수리터럴이 만들어지는 순간 자기 주변 상황을 함께 저장함.
    // i 매개변수값을 복사해 가지고 있음.


    // 함수 리터럴에 리시버를 적용하여 확장함수처럼 가능함. 개꿀
    fun functionLiteralWithReceiver(){

        val makeSure:Int.(left:Int, right:Int) -> Int

        makeSure = { left: Int, right: Int ->
            when {
                this < left -> left
                this > right -> right
                else -> this
            }
        }

        Log.d("결과" , 30.makeSure(15,40).toString())
    }

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