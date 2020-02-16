package com.project.chattie.ui.contacts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.api.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.project.chattie.R
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.ext.inflate
import kotlinx.android.synthetic.main.item_user.view.*
import kotlinx.android.synthetic.main.swipe_refresh_list.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ContactsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var listener: OnContactSelectedListener? = null

    private val contactViewModel by sharedViewModel<ContactsViewModel>()

    private val contactObserver = Observer<Outcome<List<User>>> {
        when (it) {
            is Outcome.Progress -> swipeRefresh.isRefreshing = it.loading
            is Outcome.Failure -> toast(R.string.err_something_wrong)
            is Outcome.Success -> loadContacts(it.data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContactSelectedListener) listener = context
        else throw RuntimeException("$context must implement OnContactSelectedListener")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        contactViewModel.contacts.observe(viewLifecycleOwner, contactObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.swipe_refresh_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commonList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = ContactAdapter { onContactSelected(it) }
        }
    }

    override fun onRefresh() {
        contactViewModel.fetchContacts()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun onContactSelected(user: User) {
        listener?.onContactSelected(user)
    }

    private fun loadContacts(contacts: List<User>) {
        getAdapter().addContacts(contacts)
    }

    private fun getAdapter() = commonList.adapter as ContactAdapter

    private class ContactAdapter(
        private val contacts: ArrayList<User> = arrayListOf(),
        private val itemClick: (User) -> Unit
    ) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

        fun addContacts(newContacts: List<User>) {
            contacts.clear()
            contacts.addAll(newContacts)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_user), itemClick)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bindContact(contacts[position])

        override fun getItemCount(): Int = contacts.size

        class ViewHolder(v: View, private val itemClick: (User) -> Unit) :
            RecyclerView.ViewHolder(v) {

            fun bindContact(contact: User) = with(contact) {
                itemView.avatar.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_account_circle_grey_500_48dp)
                    error(R.drawable.ic_account_circle_grey_500_48dp)
                    transformations(CircleCropTransformation())
                }

                itemView.username.text = name
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }

    interface OnContactSelectedListener {
        fun onContactSelected(user: User)
    }

}