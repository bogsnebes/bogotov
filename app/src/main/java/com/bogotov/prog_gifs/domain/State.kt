package com.bogotov.prog_gifs.domain

data class State<T>(val event: Event, val throwable: Throwable? = null, val data: T? = null)
