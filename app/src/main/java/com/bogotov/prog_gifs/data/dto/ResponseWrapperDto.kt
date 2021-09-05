package com.bogotov.prog_gifs.data.dto

/** Wrapper под запросы для [PageSection.HOT], [PageSection.LATEST], [PageSection.TOP] */
data class ResponseWrapperDto(val result: Collection<GifDto>, val totalCount: Int)
