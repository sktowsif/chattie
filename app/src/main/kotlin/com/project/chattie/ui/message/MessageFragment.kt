package com.project.chattie.ui.message

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.chattie.R
import com.project.chattie.data.Message
import com.project.chattie.ext.inflate
import com.project.chattie.ext.toPattern
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.item_receiver_message.view.*
import kotlinx.android.synthetic.main.item_sender_message.view.*
import kotlinx.android.synthetic.main.view_input_area.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MessageFragment : Fragment() {

    private val messageViewModel: MessageViewModel by sharedViewModel()

    private val newMessageObserver = Observer<Pair<Message.Action, Any>> {
        if (it.first == Message.Action.ADD) getAdapter().addMessage(it.second as Message)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        messageViewModel.newMessage.observe(viewLifecycleOwner, newMessageObserver)
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
        messageList.adapter = MessageAdapter()

        imgBtnSendMessage.setOnClickListener {
            val message = txtInputMessage.text.trim().toString()
            if (message.isNotEmpty()) {
                messageViewModel.sendMessage(message)
                txtInputMessage.setText("")
            }
        }
    }

    private fun getAdapter() = messageList.adapter as MessageAdapter

    private class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        companion object {
            private const val VIEW_TYPE_RIGHT_ALIGN = 1001
            private const val VIEW_TYPE_LEFT_ALIGN = 2002
        }

        private val messages = LinkedHashMap<String, Message>()
        private lateinit var recyclerView: RecyclerView

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
        }

        fun addMessage(newMessage: Message) {
            if (!messages.containsKey(newMessage.id)){
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
                VIEW_TYPE_RIGHT_ALIGN -> RightAlignViewHolder(parent.inflate(R.layout.item_sender_message))
                else -> LeftAlignViewHolder(parent.inflate(R.layout.item_receiver_message))
            }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) =
            holder.bindMessage(messages.values.toList()[position])

        override fun getItemCount(): Int = messages.values.size

        abstract class MessageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            abstract fun bindMessage(message: Message)
        }

        class LeftAlignViewHolder(v: View) : MessageViewHolder(v) {
            override fun bindMessage(message: Message) {
                itemView.lblMessageReceived.text = message.message
                itemView.lblMessageReceivedTime.text = message.timestamp.toPattern("hh:mm aa")
            }
        }

        class RightAlignViewHolder(v: View) : MessageViewHolder(v) {
            override fun bindMessage(message: Message) {
                itemView.lblMessageSent.text = message.message
                itemView.lblMessageSentTime.text = message.timestamp.toPattern("hh:mm aa")
            }
        }
    }
}