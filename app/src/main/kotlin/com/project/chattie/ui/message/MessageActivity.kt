package com.project.chattie.ui.message

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.lifecycle.Observer
import coil.api.load
import coil.transform.CircleCropTransformation
import com.project.chattie.R
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.ext.setSpannedText
import com.project.chattie.ext.show
import com.project.chattie.ui.login.SessionManager
import kotlinx.android.synthetic.main.common_appbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.koin.androidx.viewmodel.ext.android.viewModel

class MessageActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_USER_UID = "user_uid"

        fun create(context: Context, user: User) =
            context.intentFor<MessageActivity>(EXTRA_USER_UID to user.uid)
    }

    private val messageViewModel by viewModel<MessageViewModel>()

    private val connectedContactObserver = Observer<Outcome<User>> {
        if (it is Outcome.Success) {
            customToolbarImg.show()
            customToolbarImg.load(it.data.imageUrl) {
                placeholder(R.drawable.ic_account_circle_grey_500_48dp)
                error(R.drawable.ic_account_circle_grey_500_48dp)
                transformations(CircleCropTransformation())
            }

            customToolbarTitle.show()
            customToolbarTitle.setSpannedText(buildSpannedString {
                append(it.data.name)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.common_container_with_appbar)

        val uid = SessionManager.getUserUid(this)
        val connectedToUid = intent.getStringExtra(EXTRA_USER_UID)!!

        val chatId = createChatId(uid, connectedToUid)

        setSupportActionBar(find(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        messageViewModel.connectedContact.observe(this, connectedContactObserver)
        messageViewModel.fetchContact(connectedToUid)

        messageViewModel.fetchConversation(chatId)

    }

    private fun createChatId(senderId: String, receiverId: String): String =
        if (senderId > receiverId) senderId + receiverId else receiverId + senderId
}