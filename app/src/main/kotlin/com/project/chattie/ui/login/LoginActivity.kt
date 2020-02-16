package com.project.chattie.ui.login

import android.os.Bundle
import androidx.lifecycle.Observer
import com.project.chattie.R
import com.project.chattie.base.BaseActivity
import com.project.chattie.dashboard.DashboardActivity
import com.project.chattie.data.Outcome
import com.project.chattie.data.User
import com.project.chattie.ext.addFragment
import com.project.chattie.ext.gone
import com.project.chattie.ext.show
import kotlinx.android.synthetic.main.common_container.*
import org.jetbrains.anko.startActivity
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity(), LoginFragment.OnUserSelectedListener {

    private val loginViewModel by viewModel<LoginViewModel>()

    private val userObserver = Observer<Outcome<List<User>>> {
        when (it) {
            is Outcome.Progress -> isProcessing(it.loading)
            is Outcome.Failure -> handleError(it.e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory(lifecycleScope)
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn(this)) jumpToDashboard()

        setContentView(R.layout.common_container)
        loginViewModel.getUsers().observe(this, userObserver)

        if (savedInstanceState == null) {
            addFragment(R.id.container, LoginFragment::class.java)
        }
    }

    override fun isProcessing(isLoading: Boolean) {
        if (isLoading) progressBar.show()
        else progressBar.gone()
    }

    override fun handleError(ex: Throwable) {

    }

    override fun onUserSelected(user: User) {
        SessionManager.setLoggedUser(this, user)
        jumpToDashboard()
    }

    private fun jumpToDashboard() {
        startActivity<DashboardActivity>()
        finish()
    }

}