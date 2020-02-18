package com.project.chattie.ui.message

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.chattie.R
import com.project.chattie.ext.addFragment
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_CHAT_ID = "chat_id"
        fun create(context: Context, chatId: String) =
            context.intentFor<ChatActivity>(EXTRA_CHAT_ID to chatId)
    }

    private val messageViewModel: MessageViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory(lifecycleScope)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.common_container_with_appbar)
        setSupportActionBar(find(R.id.toolbar))

        val chatId = intent.getStringExtra(EXTRA_CHAT_ID)!!

        if (savedInstanceState == null) addFragment(R.id.container, MessageFragment::class.java)

        //messageViewModel.attachListener(chatId)
    }

}