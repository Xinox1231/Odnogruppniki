package com.bignerdranch.android.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.chat.R
import com.bignerdranch.android.chat.User
import com.bignerdranch.android.chat.databinding.ItemChatsBarBinding

class UserListAdapter(val listener: Listener) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    val users = ArrayList<User>()
    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = ItemChatsBarBinding.bind(item)
        fun bind(user: User, listener: Listener) = with(binding) {
            itemDisplayName.text = user.displayName
            itemView.setOnClickListener{
                listener.onClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chats_bar, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users.get(position), listener)
    }

    fun addUser(user: User) {
        users.add(user)
    }
    fun addUsers(users : ArrayList<User>){
        this.users.addAll(users)
    }

    interface Listener{
        fun onClick(user: User)
    }

}