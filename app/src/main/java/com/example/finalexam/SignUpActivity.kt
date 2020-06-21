package com.example.finalexam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        SignUpCompleteButton.setOnClickListener {
            createEmailId()
        }

    }

    fun createEmailId(){
        var email = SignUpEmailText.text.toString()
        var password = SignUpPasswordText.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                println("signup success")
                finish()
            }else{
                Toast.makeText(applicationContext,"이메일 형식으로 해주세요",Toast.LENGTH_LONG).show()
            }
        }
    }
}