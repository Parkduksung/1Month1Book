package com.example.a1month1book.kotlin200

import android.util.Log

object Part2 {

    // === , !== 연산자
    fun `51`() {

        var a = "one"
        var b = "one"

        //두 참조변수가 동일한 객체를 가리키면 true
        if (a === b) {
            Log.d("결과", "동일한 참조 객체")
        }


        b = "on"
        b += "e"

        // a = one 기존 가리키는데 b 는 새로운 on+e 인 one 를 가리키기 때문에 다르다.

        //true 나옴.
        print(a !== b)

        //여기서 같게 해줄려면 동일한 객체를 참조하게 만들면 된다.
        // on+e 로 만들어진 one 는 미아 객체가 되어 버렸고 이는 GC에서 정리해줄 것이다.
        b = a
    }


    fun getterSetter() {
        // get set 을 지정하여 customizing 할 수 있다.
        class Person {
            var age: Int = 0
                get() {
                    return field
                } // 여긴 생략이 가능.
                set(value) {
                    field = if (value > 0) value else 0
                }

            var name = ""
                get() = "이름 : $field"
                set(value) {
                    field = if (value.length == 0) {
                        "이름없음"
                    } else {
                        value
                    }
                }
        }// 여기서 val 형식은 초기값이 주어지면 값이 변할수 없으므로 val 형식은 getter 만 존재한다.


        val person = Person()

        person.age = -1
        person.name = ""

        Log.d("결과", "나이 : ${person.age} ${person.name}")

        person.apply {
            age = 29
            name = "박덕성"
        }

        Log.d("결과", "나이 : ${person.age} ${person.name}")
    }

    //연산자 오버로딩.
    fun operatorOverloading() {
//        class Point(var xPoint: Int = 0, var yPoint: Int = 0)
//
//        val pt1 = Point(1, 2)
//        val pt2 = Point(-4, 3)
//        val pt3 = pt1 + pt2
        // 이렇게 에러가 뜬다. 이걸 해결하고 싶을땐 어떻게 하냐...
        // 일단은 함수를 만들어야 할듯..? 그러면 분명 pt1.plus(pt2) 이런식으로 될텐데 너무 보기싫음.
        // 연산자 오버로딩 함수를 만들면되지!
        // + 가 나올때 이 함수를 쓰게 해주세요 이렇게 fun 앞에 operator 를 선언하여 사용하면되지.

        class OperatorPoint(var xPoint: Int = 0, var yPoint: Int = 0) {
            operator fun plus(other: OperatorPoint): OperatorPoint {
                return OperatorPoint(xPoint + other.xPoint, yPoint + other.yPoint)
            }
        }

        val operatorPt1 = OperatorPoint(3, 5)
        val operatorPt2 = OperatorPoint(1, -2)
        val operatorPt3 = operatorPt1 + operatorPt2

        Log.d("결과", "${operatorPt3.xPoint} ${operatorPt3.yPoint}")

        // 머 이런식으로 연산자들 오버로딩 해서 사용하면된다. 다른것들 빼기, 나누기, 곱하기 등등.
    }

    // 번호 붙은 접근 연산자.
    fun indexedAccessOperator() {

//        class Person(var name: String, var birthday: String)
//
//        val person = Person("Kotlin", "2016-02-15")
//        person[0] //오류걸림. 이걸 해결해보자.


        class Person1(var name: String, var birthday: String) {
            operator fun get(position: Int): String {
                return when (position) {
                    0 -> name
                    1 -> birthday
                    else -> "알수없음"
                }
            }

            operator fun set(position: Int, value: String) {
                when (position) {
                    0 -> name = value
                    1 -> birthday = value
                }
            }
        }

        val person1 = Person1("Kotlin", "2016-02-15")
        Log.d("결과", person1[0])
        Log.d("결과", person1[1])
        Log.d("결과", person1[-1])
    }

    //호출 연산자.
    //()는 함수를 호출할 때 사용하는 연산자이고 이를 코틀린에서는 오버로딩 가능하다.
    fun invokeOperator() {

        class Product(var name: String, var price: Int) {
            operator fun invoke() {
                Log.d("결과", "$name $price")
            }

            operator fun invoke(value: Int) {
                Log.d("결과", "$name ${price + value}")
            }
        }

        val product = Product("아메리카노", 1500)
        product()
        product(500)

    }

    // in 연산자.
    fun operatorIn() {
        val t = 'o' in "Kotlin"
        val f = "in" !in "Kotlin"
        Log.d("결과", "$t")
        Log.d("결과", "$f")
    }

    //중위표기법으로 하고싶은 경우.
    fun infixNotation() {

        class Point(var x: Int, var y: Int) {

            infix fun from(base: Point): Point =
                Point(x - base.x, y - base.y)

        }

        //원래는 Point(3,6).from(Point(1,1)) 요런식으로 표현되는것을 좀 더 직관적으로 표현하는데 좋다.
        val pt = Point(3, 6) from Point(1, 1)
        Log.d("결과", "${pt.x} , ${pt.y}")

    }


    // 상속인데.. 추상클래스 말고 기존에 클래스를 확장시켜서 사용하고 싶을때 확장시킬 클래스 앞에 open 이라 붙이면 된다.
    // 근데 이게 사용될련지..!?
    fun openClass() {

        //슈퍼클래스
        open class Person(val name: String, val age: Int)

        //서브클래스
        class Student(name: String, age: Int, val id: Int) : Person(name, age)

        val person = Person("박덕성", 29)
        val student = Student("덕성", 28, 1)

    }

    // kotlin 에서는 override 키워드 반드시 붙여줘야 한다.
    fun overriding() {

        open class AAA {
            open fun func() = Log.d("결과", "AAA")
        }

        class BBB : AAA() {
            final override fun func(): Int {
                super.func()
                return Log.d("결과", "BBB")
            }
        }

        //에러남.
//        class CCC : BBB(){
//            override fun func(): Int {
//                super.func()
//                return Log.d("결과", "BBB")
//            }
//        }

        AAA().func()
        BBB().func()

        // 만약에 BBB 다음으로 CCC 가 있고 BBB에서 final 이라고 override 된 함수앞에 명시하게되면 CCC 에서 사용할 수 없게된다.
    }

    //어떤 클래스가 아무 클래스도 상속하지 않으면 자동으로 Any 라는 클래스를 상속한다. 다른 클래스를 상속한다 해도 그 클래스 또한 Any 클래스를 자동으로 상속하므로
    //간접적으로 Any 클래스를 상속하게 된다.
    // 즉, 모든 코틀린 클래스들은 Any 클래스를 상속한다는 것이 보장된다.
    fun anyClass() {

    }

    //엘비스 연산자는 왼쪽의 피연사자가 null이 아니면 그 값을 그대로 쓰고, null 이면 우측의 피연산자로 대체하는 매우 유용한 연산자이다.
    fun elvisOperator() {

        // null 이 존재할수 있을때 사용하면 좀더 직관적으로 보일려나..?

        val count: Int? = null

        count?.let { Log.d("결과", it.toString()) } ?: Log.d("결과", "null 이야")
    }


    // 접근한정자인데 그냥 다시한번 알아두는게 좋을거 같아서..
    fun protected() {

        open class Person(protected val name: String, protected val age: Int)


        class Student(name: String, age: Int) : Person(name, age) {

            fun printName() {
                Log.d("결과", name)
            }
        }

        val student = Student("박덕성", 29)
        student.printName()
    }


    // 진짜 자주 사용하면 편리하지.. 매번 상속해서 사용하는게 아니고 리시버타입에 . 붙여서 사용만 하면 되니까..
    fun extensionFunction() {

        fun String.isNumber(): Boolean {

            var i = 0

            this.forEach {
                if (!it.isDigit()) {
                    i += 1
                }
            }
            return i == 0
        }


        val exampleString1 = "1234095"
        val exampleString2 = "1v23409e5"

        Log.d("결과", exampleString1.isNumber().toString())
        Log.d("결과", exampleString2.isNumber().toString())
    }


    // 함수 말고 변수로도 확장할 수 있다.
    fun extensionProperty() {

        Log.d("결과", "exampleString12341234".isLarge.toString())
        Log.d("결과", "exam".isLarge.toString())
    }

    private val String.isLarge: Boolean
        get() = this.length >= 10

}

