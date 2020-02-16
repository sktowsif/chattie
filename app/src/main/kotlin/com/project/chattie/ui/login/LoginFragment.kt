package com.project.chattie.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.project.chattie.R
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.ext.inflate
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_user.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment : Fragment() {

    private var listener: OnUserSelectedListener? = null

    private val loginViewModel by sharedViewModel<LoginViewModel>()

    private val userObserver = Observer<Outcome<List<User>>> {
        if (it is Outcome.Success) loadUserDetails(it.data)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUserSelectedListener) listener = context
        else throw RuntimeException("$context must implement OnUserSelectedListener")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginViewModel.getUsers().observe(viewLifecycleOwner, userObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun loadUserDetails(users: List<User>) {
        userDetailContainer.removeAllViews()
        users.forEach {
            val userView = createUserUIComponent(userDetailContainer, it)
            userDetailContainer.addView(userView)
        }
    }

    private fun createUserUIComponent(container: ViewGroup, user: User): View {
        val convertView = container.inflate<LinearLayout>(R.layout.item_user)
        convertView.username.text = user.name
        // TODO : Update profile image
        convertView.avatar.setImageResource(R.drawable.ic_account_circle_grey_500_48dp)
        convertView.setOnClickListener { listener?.onUserSelected(user) }
        return convertView
    }

    interface OnUserSelectedListener {
        fun onUserSelected(user: User)
    }

}