package com.bignerdranch.android.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etEmail : TextView
    lateinit var btnNext : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        btnNext = findViewById(R.id.forgot_password_btn)
        etEmail = findViewById(R.id.forgot_password_et_email)

        btnNext.setOnClickListener{
            when{
                TextUtils.isEmpty(etEmail.text.toString().trim { it <=  ' '}) ->{
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Введите e-mail", Toast.LENGTH_SHORT
                    ).show()
                }else ->{
                    val email: String = etEmail.text.toString()

                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener{
                        task ->
                        if(task.isSuccessful){
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Письмо отправлено на ваш e-mail адрес", Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                task.exception!!.message.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}