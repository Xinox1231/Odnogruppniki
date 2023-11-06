package com.bignerdranch.android.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var tv_user_id : TextView
    lateinit var tv_email : TextView
    lateinit var btn_logout : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userId = intent.getStringExtra("user_id")
        val email = intent.getStringExtra("email")

        tv_user_id = findViewById(R.id.main_tv_user_id)
        tv_email = findViewById(R.id.main_tv_user_email)
        btn_logout = findViewById(R.id.main_btn_logout)

        tv_user_id.text = "User ID :: $userId"
        tv_email.text = "Email ID :: $email"

        btn_logout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }


    }


}