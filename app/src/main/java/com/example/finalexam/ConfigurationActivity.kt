package com.example.finalexam

import android.content.ContentProviderClient
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class ConfigurationActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //버튼에 로그아웃 기능을 넣는다.
        signOutButton.setOnClickListener {
            signOut()
        }
        //세션 로그아웃 구현
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //구글 클라이언트를 연결시킵니다.
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        //유저 정보 가져오기
        var user = FirebaseAuth.getInstance().currentUser!!

        //파이어 베이스에서 닉네임을 지정했는지 확인합니다.
        //닉네임을 정했으면 MainActivity로 넘어갑니다.
        nickNameText.hint = user.displayName
        createNickButton.setOnClickListener {
            if (nickNameText.text.toString().isNullOrBlank()){
                Toast.makeText(applicationContext,"닉네임을 그대로 합니다.",Toast.LENGTH_LONG).show()
                startActivity(MainActivity.getLaunchIntent(this))
            }else{
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName("${nickNameText.text}")
                    .build()

                user?.updateProfile(profileUpdate)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext,"닉네임을 지정했습니다.",Toast.LENGTH_LONG).show()
                            user.reload()
                        }
                    }
                startActivity(MainActivity.getLaunchIntent(this))
            }
        }

    }

    // 세션 로그아웃 함수
    private fun signOut() {
        startActivity(loginActivity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut();
        // 구글 세션 로그 아웃
        googleSignInClient?.signOut()
        //페이스북 세션 로그아웃
        LoginManager.getInstance().logOut()
        finish()
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, ConfigurationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}