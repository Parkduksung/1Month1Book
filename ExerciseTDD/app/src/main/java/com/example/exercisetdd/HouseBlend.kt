package com.example.exercisetdd


abstract class Beverage {

    private val description = "Description"

    open fun cost(): Int = 0
    open fun getDescription(): String {
        return description
    }

}


class HouseBlend : Beverage() {

    override fun cost(): Int =
        super.cost() + 3500

    override fun getDescription(): String {
        return super.getDescription() + " Add HouseBlend"
    }
}
