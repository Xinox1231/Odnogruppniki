package com.bignerdranch.android.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.bignerdranch.android.chat.R
//import com.google.firebase.firestore.auth.User

class ChatAdapter(context: Context, resource: Int, private val dataList: List<User>) : ArrayAdapter<User>(context, resource, dataList) {
    private lateinit var context : Context
    private lateinit var chatsData : List<User>

    init {
        this.context = context
        this.chatsData = dataList
    }

    override fun getView(position : Int, convertView: View?, parent : ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view : View =  inflater.inflate(R.layout.item, parent, false)
        var tvDisplayName : TextView = view.findViewById(R.id.item_display_name)
        tvDisplayName.text = this.chatsData.get(position).displayName

        return view
    }
}