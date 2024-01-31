package com.bignerdranch.android.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.chat.Message
import com.bignerdranch.android.chat.R
import com.bignerdranch.android.chat.databinding.ItemChatMessageBinding

class CurrentChatAdapter : RecyclerView.Adapter<CurrentChatAdapter.ViewHolder>()  {
    val messages = ArrayList<Message>()
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item){
        val binding = ItemChatMessageBinding.bind(item)
        fun bind(message : Message) = with(binding){
            itemChatMessage.text = message.text
            itemChatMessageDisplayName.text = message.senderName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages.get(position))
    }

    fun addMessage(message: Message){
        messages.add(message)
    }
}