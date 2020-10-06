package com.rsupport.mobile.agent.utils

sealed class Result<T> {
    val isSuccess: Boolean
        get() = this is Success

    class Success<T>(val value: T) : Result<T>()
    class Failure<T>(val throwable: Throwable) : Result<T>()

    companion object {
        fun <T> success(value: T): Result<T> {
            return Success(value)
        }

        fun <T> failure(value: Throwable): Result<T> {
            return Failure(value)
        }

    }
}