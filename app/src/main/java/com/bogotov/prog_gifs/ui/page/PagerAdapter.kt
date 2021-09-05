package com.bogotov.prog_gifs.ui.page

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bogotov.prog_gifs.data.PageInfoFactory

internal class PagerAdapter(private val activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = PageInfoFactory.PAGES.map { PageFragment.newInstance(it) }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getPageTitle(position: Int): CharSequence {
        return activity.resources.getString(PageInfoFactory.PAGES.elementAt(position).resourceId)
    }
}
