package com.project.chattie.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.project.chattie.R
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.support.v4.toast
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userDetailContainer.forEach {
            view.setOnClickListener { listener?.onUserSelected(it.tag as User) }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun loadUserDetails(users: List<User>) {
        if (users.size >= userDetailContainer.childCount) {
            userDetailContainer.forEachIndexed { index, view ->
                if (view is TextView) {
                    // Set user name hold user object in tag so that we can
                    // get this object on view click
                    view.text = users[index].name
                    view.tag = users[index]
                }
            }
        } else toast(R.string.err_missing_users)
    }

    interface OnUserSelectedListener {
        fun onUserSelected(user: User)
    }

}