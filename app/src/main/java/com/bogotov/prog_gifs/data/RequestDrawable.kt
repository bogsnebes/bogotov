package com.bogotov.prog_gifs.data

import com.bumptech.glide.request.RequestListener

/** Интерфейс для упрощённого взаимодействия с [RequestListener] */
interface RequestDrawable {
    fun onLoadFailed()
    fun onResourceReady()
}
