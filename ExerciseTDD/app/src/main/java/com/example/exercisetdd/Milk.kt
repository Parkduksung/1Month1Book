package com.example.exercisetdd


class Milk(beverage: Beverage) : CondimentDecorator(beverage) {

    override fun cost(): Int =
        super.cost() + 500
}


abstract class CondimentDecorator(private val beverage: Beverage) : Beverage() {
    override fun cost(): Int {
        return super.cost() + beverage.cost()
    }
}