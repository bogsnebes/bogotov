package com.bogotov.prog_gifs.data

import com.bogotov.prog_gifs.R
import com.bogotov.prog_gifs.domain.PageInfo
import com.bogotov.prog_gifs.domain.PageSection

/** Фабрика страниц. */
class PageInfoFactory {
    companion object {

        val PAGES: Set<PageInfo> =
            setOf(
                PageInfo(R.string.tab_random, PageSection.RANDOM),
                PageInfo(R.string.tab_top, PageSection.TOP),
                PageInfo(R.string.tab_latest, PageSection.LATEST)
            )
    }
}
