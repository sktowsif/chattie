package com.project.chattie.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.project.chattie.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    enum class Tab(@StringRes val labelResId: Int) {
        CONTACT(R.string.tab_contact), GROUP(R.string.tab_group)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = Tab.values().asList()
        homePager.adapter = ChatPagerAdapter(this, tabs)

        TabLayoutMediator(homeTabs, homePager) { tab, position ->
            tab.text = getString(tabs[position].labelResId)
        }.attach()

        // For demo purpose only
        val badge = homeTabs.getTabAt(1)?.orCreateBadge
        badge?.isVisible = true
        badge?.number = 120
    }

    private class ChatPagerAdapter(fragment: Fragment, private val tabs: List<Tab>) :
        FragmentStateAdapter(fragment) {

        override fun createFragment(position: Int): Fragment = when (tabs[position]) {
            Tab.CONTACT -> ChatsFragment.newInstance()
            Tab.GROUP -> GroupsFragment.newInstance()
        }

        override fun getItemCount(): Int = tabs.size

    }

}