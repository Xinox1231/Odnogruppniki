package com.bignerdranch.android.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.chat.R
import com.bignerdranch.android.chat.data_classes.Chat
import com.bignerdranch.android.chat.databinding.ItemChatsBarBinding

class UserListAdapter(val listener: Listener) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    val chats = ArrayList<Chat>()
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = ItemChatsBarBinding.bind(item)

        fun bind(chat: Chat, listener: Listener) = with(binding) {
            itemDisplayName.text = chat.otherUserDisplayName
            itemLatestMessage.text = chat.latestMessage

            itemView.setOnClickListener {
                listener.onClick(chat)
            }
        }
    }


    fun addChat(chat: Chat){
        chats.add(chat)
        notifyItemInserted(itemCount - 1)
    }

    fun updateLatestMessage(chatPosition: Int, latestMessage: String) {
        if (chatPosition in 0 until chats.size) {
            chats[chatPosition].latestMessage = latestMessage
            notifyItemChanged(chatPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chats_bar, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chats.get(position), listener)
    }

    interface Listener {
        fun onClick(chat: Chat)
    }


}