package com.bignerdranch.android.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {
    lateinit var buttonRegister: Button
    lateinit var textEmail: TextView
    lateinit var textPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        buttonRegister = findViewById(R.id.signup_btn_signup)
        textEmail = findViewById(R.id.signup_tv_email)
        textPassword = findViewById(R.id.signup_tv_password)

        buttonRegister.setOnClickListener {
            when {
                TextUtils.isEmpty(textEmail.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Введите e-mail",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(textPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Введите пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {

                    val email : String = textEmail.text.toString().trim{ it <= ' '}
                    val password : String = textPassword.text.toString().trim{ it <= ' '}

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                val firebaseUser : FirebaseUser = task.result!!.user!!

                                Toast.makeText(this@SignUpActivity,
                                "Вы зарегистрировались", Toast.LENGTH_LONG).show()

                                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", firebaseUser.uid)
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this@SignUpActivity,
                                task.exception!!.message.toString(),
                                Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }
    }
}