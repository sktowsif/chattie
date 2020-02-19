package com.project.chattie.ui.message

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import androidx.lifecycle.Observer
import coil.api.load
import coil.transform.CircleCropTransformation
import com.project.chattie.R
import com.project.chattie.data.Message
import com.project.chattie.data.Outcome
import com.project.chattie.data.Role
import com.project.chattie.data.User
import com.project.chattie.ext.*
import com.project.chattie.ui.login.SessionManager
import com.project.chattie.ui.message.MessageFragment.Companion.BUNDLE_CHAT_ID
import kotlinx.android.synthetic.main.common_appbar.*
import kotlinx.android.synthetic.main.common_container.*
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit

class MessageActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_CHAT_UID = "chat_id"
        private const val EXTRA_CONNECTED_UID = "user_uid"

        fun findMessage(context: Context, uid: String) =
            context.intentFor<MessageActivity>(EXTRA_CONNECTED_UID to uid)

        fun openChat(context: Context, chatId: String, uid: String? = null) =
            context.intentFor<MessageActivity>(
                EXTRA_CHAT_UID to chatId,
                EXTRA_CONNECTED_UID to uid
            )
    }

    private val messageViewModel by viewModel<MessageViewModel>()

    private val receiverContactObserver = Observer<Outcome<User>> {
        if (it is Outcome.Success) {
            customToolbarImg.show()
            customToolbarImg.load(it.data.imageUrl) {
                placeholder(R.drawable.ic_account_circle_grey_500_48dp)
                error(R.drawable.ic_account_circle_grey_500_48dp)
                transformations(CircleCropTransformation())
            }
        }
    }

    private val statusChangeObserver = Observer<Triple<String, Boolean, Long>> {
        it?.run {
            customToolbarTitle.show()
            customToolbarTitle.setSpannedText(buildSpannedString {
                append(it.first)
                scale(0.5F) {
                    if (it.second) append("\n${getString(R.string.online)}")
                    else if (it.third > 0) append("\n${formatTime(it.third)}")
                }
            })
        }
    }

    private fun formatTime(timeInMills: Long): String {
        val lastSeenDate = Date(timeInMills).toCalendar()
        val strLastSeen = lastSeenDate.toPattern("dd-MM-yyyy")
        val nowDate = Calendar.getInstance()
        val strNowDate = nowDate.toPattern("dd-MM-yyyy")
        return if (strLastSeen == strNowDate) lastSeenDate.toPattern("hh:mm aa")
        else {
            val difference = nowDate.timeInMillis - lastSeenDate.timeInMillis
            val daysBetween = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS)
            if (daysBetween > 7) lastSeenDate.toPattern("dd MMM hh:mm aa")
            else lastSeenDate.toPattern("EEE hh:mm aa")
        }
    }

    private val messageObserver = Observer<Outcome<Pair<List<User>, List<Message>>>> { outcome ->
        when (outcome) {
            is Outcome.Success -> {
                customToolbarTitle.show()
                customToolbarTitle.setSpannedText(buildSpannedString {
                    append(outcome.data.first.map { it.name }.joinToString(" & "))
                })
            }
            is Outcome.Progress -> isProcessing(outcome.loading)
            is Outcome.Failure -> toast(R.string.err_something_wrong)
        }
    }

    private val updateMessageObserver = Observer<Outcome<Pair<String, String>>> {
        when (it) {
            is Outcome.Progress -> isProcessing(it.loading)
            is Outcome.Failure -> toast(R.string.err_something_wrong)
        }
    }

    private fun isProcessing(isLoading: Boolean) {
        if (isLoading) progressBar.show()
        else progressBar.gone()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory(lifecycleScope)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.common_container_with_appbar)
        setSupportActionBar(find(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userRole = SessionManager.getUserRole(this)
        val chatId = intent.getStringExtra(EXTRA_CHAT_UID)

        if (savedInstanceState == null) addFragment(
            R.id.container,
            MessageFragment::class.java,
            args = bundleOf(BUNDLE_CHAT_ID to chatId)
        )

        if (userRole == Role.ADMIN) {
            messageViewModel.fetchMessages(chatId)
            messageViewModel.messages.observe(this, messageObserver)
            messageViewModel.onMessageUpdate().observe(this, updateMessageObserver)
        } else {
            messageViewModel.receiverContactDetail.observe(this, receiverContactObserver)
            messageViewModel.statusChange.observe(this, statusChangeObserver)

            val connectedUid = intent.getStringExtra(EXTRA_CONNECTED_UID)!!
            messageViewModel.fetchReceiverContactDetail(connectedUid)

            if (chatId.isNullOrEmpty()) messageViewModel.findConversation(connectedUid)
            else messageViewModel.attachMessageNodeListener(chatId, connectedUid)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}