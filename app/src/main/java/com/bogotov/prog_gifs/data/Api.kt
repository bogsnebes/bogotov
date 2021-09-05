package com.bogotov.prog_gifs.data

import com.bogotov.prog_gifs.data.dto.GifDto
import com.bogotov.prog_gifs.data.dto.ResponseWrapperDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/** Интерфейс для взаимодействия с интернет-ресурсом. */
interface Api {

    /**
     * Метод для получения гиф изображений по разделу.
     *
     * @param
     * - раздел.
     * @param
     * - страница раздела.
     */
    @GET("/{section}/{page}?json=true")
    fun getSectionGIFs(
        @Path("section") section: String,
        @Path("page") page: Int,
    ): Call<ResponseWrapperDto>

    /** Метод для получения случайного гиф изображения. */
    @GET("/random?json=true") fun getRandomGif(): Call<GifDto>
}
