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
import com.project.chattie.data.Chat
import com.project.chattie.data.Outcome
import com.project.chattie.ext.gone
import com.project.chattie.ext.inflate
import com.project.chattie.ext.show
import com.project.chattie.ext.toPattern
import com.project.chattie.ui.login.SessionManager
import kotlinx.android.synthetic.main.common_list.*
import kotlinx.android.synthetic.main.item_chat.view.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChatsFragment : Fragment() {

    companion object {
        fun newInstance() = ChatsFragment()
    }

    private var listener: OnChatSelectedListener? = null
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()

    private val chatObserver = Observer<Outcome<List<Chat>>> {
        when (it) {
            is Outcome.Progress -> isProcessing(it.loading)
            is Outcome.Failure -> toast(R.string.err_something_wrong)
            is Outcome.Success -> loadChats(it.data)
        }
    }

    private fun isProcessing(isLoading: Boolean) {
        if (isLoading) commonListProgress.show()
        else commonListProgress.gone()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChatSelectedListener) listener = context
        else throw RuntimeException("$context must implement OnChatSelectedListener")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dashboardViewModel.chats.observe(viewLifecycleOwner, chatObserver)
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
            adapter = ChatAdapter(context!!) { onChatSelected(it) }
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

    private fun loadChats(chats: List<Chat>) {
        getAdapter().addChats(chats)
    }

    private fun getAdapter() = commonList.adapter as ChatAdapter

    private class ChatAdapter(
        context: Context,
        private val chats: ArrayList<Chat> = arrayListOf(),
        private val itemClick: (Chat) -> Unit
    ) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

        private val senderName = SessionManager.getUsername(context)

        fun addChats(newChats: List<Chat>) {
            chats.clear()
            chats.addAll(newChats)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_chat), itemClick)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bindChat(chats[position])

        override fun getItemCount(): Int = chats.size

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