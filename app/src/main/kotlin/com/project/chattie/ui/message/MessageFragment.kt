package com.project.chattie.ui.message

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.project.chattie.R
import com.project.chattie.data.*
import com.project.chattie.ext.inflate
import com.project.chattie.ext.show
import com.project.chattie.ui.login.SessionManager
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.item_receiver_message.view.*
import kotlinx.android.synthetic.main.item_sender_message.view.*
import kotlinx.android.synthetic.main.view_input_area.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MessageFragment : Fragment() {

    companion object {
        const val BUNDLE_CHAT_ID = "chat_id"
    }

    private val messageViewModel: MessageViewModel by sharedViewModel()
    private val userRole by lazy { SessionManager.getUserRole(context!!) }

    private val newMessageObserver = Observer<Pair<Action, Any>> { message ->
        message?.run {
            if (first == Action.ADD) getAdapter().addMessage(second as Message)
        }
    }

    private val messageObserver = Observer<Outcome<Pair<List<User>, List<Message>>>> {
        if (it is Outcome.Success) getAdapter().addMessages(it.data.second)
    }

    private val updateMessageObserver = Observer<Outcome<Pair<String, String>>> {
        if (it is Outcome.Success) {
            getAdapter().updateMessage(it.data.first, it.data.second)
        }
    }

    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        chatId = arguments?.getString(BUNDLE_CHAT_ID)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (userRole == Role.ADMIN) {
            messageViewModel.messages.observe(viewLifecycleOwner, messageObserver)
            messageViewModel.onMessageUpdate().observe(viewLifecycleOwner, updateMessageObserver)
        } else messageViewModel.newMessage.observe(viewLifecycleOwner, newMessageObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageList.layoutManager = LinearLayoutManager(context)
        messageList.adapter = MessageAdapter(context!!) { openEditMessageDialog(it) }

        imgBtnSendMessage.setOnClickListener {
            val message = txtInputMessage.text.trim().toString()
            if (message.isNotEmpty()) {
                messageViewModel.sendMessage(message)
                txtInputMessage.setText("")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_menu_message, menu)
    }

    private fun openEditMessageDialog(message: Message) {
        MaterialDialog(context!!)
            .title(text = "Edit Message")
            .input(
                prefill = message.message,
                inputType = InputType.TYPE_CLASS_TEXT,
                allowEmpty = false,
                waitForPositiveButton = true
            )
            .positiveButton {
                val newMessage = it.getInputField().text.trim().toString()
                messageViewModel.onEditMessage(chatId!!, message.id!!, newMessage)
                it.dismiss()
            }
            .show()
    }

    private fun getAdapter() = messageList.adapter as MessageAdapter

    private class MessageAdapter(context: Context, private val itemClick: (Message) -> Unit) :
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        companion object {
            private const val VIEW_TYPE_RIGHT_ALIGN = 1001
            private const val VIEW_TYPE_LEFT_ALIGN = 2002
        }

        private val userRole = SessionManager.getUserRole(context)

        private val messages = LinkedHashMap<String, Message>()
        private lateinit var recyclerView: RecyclerView

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
        }

        fun addMessages(newMessages: List<Message>) {
            messages.clear()
            newMessages.map { messages[it.id!!] = it }
            notifyDataSetChanged()
        }

        fun updateMessage(messageId: String, updatedMessage: String) {
            val index = messages.values.indexOfFirst { it.id == messageId }
            messages.values.toList()[index].message = updatedMessage
            notifyItemChanged(index)
        }

        fun addMessage(newMessage: Message) {
            if (!messages.containsKey(newMessage.id)) {
                messages[newMessage.id!!] = newMessage
                val position = messages.values.size - 1
                notifyItemInserted(position)
                recyclerView.scrollToPosition(position)
            }
        }

        override fun getItemViewType(position: Int): Int {
            val message = messages.values.toList()[position]
            return if (message.align == Paint.Align.LEFT) VIEW_TYPE_LEFT_ALIGN
            else VIEW_TYPE_RIGHT_ALIGN
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder =
            when (viewType) {
                VIEW_TYPE_RIGHT_ALIGN -> RightAlignViewHolder(
                    parent.inflate(R.layout.item_sender_message),
                    itemClick
                )
                else -> LeftAlignViewHolder(
                    parent.inflate(R.layout.item_receiver_message),
                    itemClick
                )
            }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
            holder.bindMessage(messages.values.toList()[position])

        override fun getItemCount(): Int = messages.values.size

        abstract class MessageViewHolder(v: View) :
            RecyclerView.ViewHolder(v) {
            abstract fun bindMessage(message: Message)
        }

        inner class LeftAlignViewHolder(v: View, private val itemClick: (Message) -> Unit) :
            MessageViewHolder(v) {
            override fun bindMessage(message: Message) {
                if (userRole == Role.ADMIN) {
                    itemView.lblMessageReceivedBy.show()
                    itemView.lblMessageReceivedBy.text = message.name
                    itemView.setOnClickListener { itemClick(message) }
                }
                itemView.lblMessageReceived.text = message.message
                itemView.lblMessageReceivedTime.text = message.strDateTime
            }
        }

        inner class RightAlignViewHolder(v: View, private val itemClick: (Message) -> Unit) :
            MessageViewHolder(v) {
            override fun bindMessage(message: Message) {
                if (userRole == Role.ADMIN) {
                    itemView.lblMessageSentBy.show()
                    itemView.lblMessageSentBy.text = message.name
                    itemView.setOnClickListener { itemClick(message) }
                }
                itemView.lblMessageSent.text = message.message
                itemView.lblMessageSentTime.text = message.strDateTime
            }
        }
    }
}