package com.bogotov.prog_gifs.data

/** Интерфейс для реализации подписчиком методов взависимости от события. */
interface EventObserver<T> {
    fun onSuccess(data: T)
    fun onLoading()
    fun onError(throwable: Throwable? = null)
}
