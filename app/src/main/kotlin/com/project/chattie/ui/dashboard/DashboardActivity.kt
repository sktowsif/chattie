package com.project.chattie.ui.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.project.chattie.R
import com.project.chattie.data.Chat
import com.project.chattie.data.Role
import com.project.chattie.ext.addFragment
import com.project.chattie.services.StatusWorker
import com.project.chattie.ui.contacts.ContactsActivity
import com.project.chattie.ui.login.SessionManager
import com.project.chattie.ui.message.MessageActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.lifecycleScope

class DashboardActivity : AppCompatActivity(), ChatsFragment.OnChatSelectedListener {

    private val userRole by lazy { SessionManager.getUserRole(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory(lifecycleScope)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.action_chats)

        if (savedInstanceState == null) addFragment(R.id.main_container, HomeFragment::class.java)

        fabSelectContact.setOnClickListener { startActivity<ContactsActivity>() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onChatSelected(chat: Chat) {
        if (userRole == Role.ADMIN) {
            startActivity(MessageActivity.openChat(this, chat.id!!))
        } else {
            startActivity(MessageActivity.openChat(this, chat.id!!, chat.uid))
        }

    }
}
