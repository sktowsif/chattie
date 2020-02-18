package com.project.chattie.ui.contacts

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.lifecycle.Observer
import com.project.chattie.R
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.ext.addFragment
import com.project.chattie.ext.quantityText
import com.project.chattie.ext.setSpannedText
import com.project.chattie.ext.show
import com.project.chattie.ui.message.MessageActivity
import org.jetbrains.anko.find
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContactsActivity : AppCompatActivity(), ContactsFragment.OnContactSelectedListener {

    private lateinit var toolbarTitle: TextView

    private val contactViewModel by viewModel<ContactsViewModel>()

    private val contactObserver = Observer<Outcome<List<User>>> {
        if (it is Outcome.Success) toolbarTitle.setSpannedText(buildSpannedString {
            append(getString(R.string.title_select_contacts))
            append("\n")
            scale(0.7F) { append(quantityText(R.plurals.pl_contacts, it.data.size)) }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory(lifecycleScope)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.common_container_with_appbar)
        setSupportActionBar(find(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbarTitle = find(R.id.customToolbarTitle)
        toolbarTitle.show()

        contactViewModel.contacts.observe(this, contactObserver)

        if (savedInstanceState == null) addFragment(R.id.container, ContactsFragment::class.java)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onContactSelected(user: User) {
        startActivity(MessageActivity.findMessage(this, user.uid!!))
        finish()
    }
}