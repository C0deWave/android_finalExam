package com.example.finalexam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*

//회원 가입을 하는 페이지의 코틀린파일입니다.

class SignUpActivity : AppCompatActivity() {

    //-----------------------------------------------------------------------------------------
    //onCreate 함수입니다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //액션바 숨기
        supportActionBar?.hide()
        //화면 전환 효과를 페이드 인 ,아웃으로 전환 합니다.
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        setContentView(R.layout.activity_sign_up)

        //회원 가입 버튼을 눌렀을때
        SignUpCompleteButton.setOnClickListener {
            createEmailId()
        }
    }//end of on Create

    //--------------------------------------------------------------------------------------------
    //회원 가입을 기능하는 함수입니다.
    fun createEmailId(){
        var email = SignUpEmailText.text.toString()
        var password = SignUpPasswordText.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                println("signup success")
                FirebaseAuth.getInstance().signOut()
                finish()
            }else{
                Toast.makeText(applicationContext,"이메일 형식으로 해주세요",Toast.LENGTH_LONG).show()
            }
        }
    }//end of createEmailId
}