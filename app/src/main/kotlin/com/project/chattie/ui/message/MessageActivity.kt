package com.project.chattie.ui.message

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.lifecycle.Observer
import com.project.chattie.R
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.ext.setSpannedText
import com.project.chattie.ext.show
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.koin.androidx.viewmodel.ext.android.viewModel

class MessageActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_USER_UID = "user_uid"

        fun create(context: Context, user: User) =
            context.intentFor<MessageActivity>(EXTRA_USER_UID to user.uid)
    }

    private lateinit var toolbarTitle: TextView

    private val messageViewModel by viewModel<MessageViewModel>()

    private val connectedContactObserver = Observer<Outcome<User>> {
        if (it is Outcome.Success) {
            toolbarTitle.setSpannedText(buildSpannedString {
                append(it.data.name)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_container_with_appbar)

        val uid = intent.getStringExtra(EXTRA_USER_UID)

        setSupportActionBar(find(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbarTitle = find(R.id.customToolbarTitle)
        toolbarTitle.show()

        messageViewModel.connectedContact.observe(this,connectedContactObserver)
        messageViewModel.fetchContact(uid)

    }
}