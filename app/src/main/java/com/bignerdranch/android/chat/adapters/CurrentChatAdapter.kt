package com.bignerdranch.android.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bignerdranch.android.chat.Message
import com.bignerdranch.android.chat.R

class ChatAdapter(context: Context, resource: Int, private val messageList: List<Message>) : ArrayAdapter<Message>(context, resource, messageList) {
    private lateinit var context : Context
    private lateinit var chatsData : List<Message>

    init {
        this.context = context
        this.chatsData = messageList
    }

    override fun getView(position : Int, convertView: View?, parent : ViewGroup): View {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view : View =  inflater.inflate(R.layout.item_chat_message, parent, false)
        var tvDisplayName : TextView = view.findViewById(R.id.item_chat_message_diplay_name)
        var tvChatData : TextView = view.findViewById(R.id.item_chat_message_data)
        tvDisplayName.text = chatsData.get(position).senderName
        tvChatData.text = chatsData.get(position).text

        return view
    }
}