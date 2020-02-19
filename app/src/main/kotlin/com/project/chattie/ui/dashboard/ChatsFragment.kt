package com.project.chattie.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.project.chattie.R
import com.project.chattie.data.Action
import com.project.chattie.data.Chat
import com.project.chattie.ext.inflate
import com.project.chattie.ext.toPattern
import kotlinx.android.synthetic.main.common_list.*
import kotlinx.android.synthetic.main.item_chat.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChatsFragment : Fragment() {

    companion object {
        fun newInstance() = ChatsFragment()
    }

    private var listener: OnChatSelectedListener? = null
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()

    private val chatObserver = Observer<Pair<Action, Chat>> { chat ->
        chat?.run {
            if (first == Action.ADD) getAdapter().addChat(second)
            else if (first == Action.CHANGE) getAdapter().updateChat(second)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChatSelectedListener) listener = context
        else throw RuntimeException("$context must implement OnChatSelectedListener")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dashboardViewModel.newChat().observe(viewLifecycleOwner, chatObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.common_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commonList.apply {
            setHasFixedSize(true)
            adapter = ChatAdapter { onChatSelected(it) }
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun onChatSelected(chat: Chat) {
        listener?.onChatSelected(chat)
    }

//    private fun loadChats(chats: List<Chat>) {
//        getAdapter().addChats(chats)
//    }

    private fun getAdapter() = commonList.adapter as ChatAdapter

    private class ChatAdapter(
        private val chats: LinkedHashMap<String, Chat> = linkedMapOf(),
        private val itemClick: (Chat) -> Unit
    ) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        fun addChat(newChat: Chat) {
            if (!chats.containsKey(newChat.id)) {
                chats[newChat.id!!] = newChat
                val position = chats.values.size - 1
                if (chats.size > 1) notifyItemInserted(position)
                else notifyDataSetChanged()
            }
        }

        fun updateChat(oldChat: Chat) {
            val index = chats.values.indexOfFirst { it.id!! == oldChat.id!! }
            if (index >= 0) {
                chats[oldChat.id!!] = oldChat
                notifyItemChanged(index)
            }
        }

        fun removeChat(oldChat: Chat) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_chat), itemClick)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bindChat(chats.values.toList()[position])

        override fun getItemCount(): Int = chats.values.size

        inner class ViewHolder(v: View, private val itemClick: (Chat) -> Unit) :
            RecyclerView.ViewHolder(v) {

            fun bindChat(chat: Chat) {
                itemView.chatTime.text = chat.timestamp.toPattern("hh:mm aa")
                itemView.chatTitle.text = chat.title
                itemView.chatMessage.text = chat.lastMessage

                itemView.chatAvatar.load(chat.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_account_circle_grey_500_48dp)
                    error(R.drawable.ic_account_circle_grey_500_48dp)
                    transformations(CircleCropTransformation())
                }

                itemView.setOnClickListener { itemClick(chat) }
            }
        }
    }

    interface OnChatSelectedListener {
        fun onChatSelected(chat: Chat)
    }

}