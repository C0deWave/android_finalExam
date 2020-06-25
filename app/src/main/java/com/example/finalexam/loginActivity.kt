package com.example.finalexam

//페이스북 로그인 연동을 위함이다.

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalexam.ui.accountConfigurationFragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class loginActivity : AppCompatActivity() {

    //구글 로그인 변수
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    var RC_SIGN_IN = 1

    //파이어베이스에서 이용하는 변수이다.
    private lateinit var firebaseAuth: FirebaseAuth

    //페이스북에서 이용
    lateinit var callbackManager: CallbackManager


    //onCreate보다 먼저 실행되는 함수이다.
    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            //이미 로그인 되어 있다면 패스한다.
            startActivity(MainActivity.getLaunchIntent(this))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //액션바 숨기
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        configureSignIn()

        googleLoginButton.setOnClickListener {
            signInGoogle()
        }

        printHashKey(this)

        facebookLoginButton.setOnClickListener {
            signInFacebook()
        }

        Login_SignUpButton.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        Login_loginButton.setOnClickListener {
            loginEmail()
        }
    }

    // 각종 변수들의 초기화를 담당하는 함수이다.
    private fun configureSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)

        //페이스북 로그인 변수 할당
        callbackManager = CallbackManager.Factory.create()
    }

    //기본 이메일로 로그인을 하는 함수이다.
    fun loginEmail(){
        val email = Login_email.text.toString()
        val password = Login_password.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                startActivity(ConfigurationActivity.getLaunchIntent(this))
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }
        }
    }

    //구글 로그인을 해주는 함수이다.
    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }


    //페이스북에서 로그인을 하게 해주는 함수이다.
    fun signInFacebook() {
        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        LoginManager.getInstance().logInWithReadPermissions(
            this, Arrays.asList(
                "public_profile", "email"
            )
        )
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    firebaseAuthWithFacebook(result)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException?) {

                }

            })
    }


    //구글에서 로그인한 정보를 파이어베이스에 연동하는 코드이다.
    fun firebaseAuthWhitGoogle(acct: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Signup success")
                //홈 액티비티로 넘어간다.
                Toast.makeText(applicationContext, "로그인에 성공했습니다.", Toast.LENGTH_LONG).show()
                startActivity(ConfigurationActivity.getLaunchIntent(this))
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else {
                //로그인 실패인 거시다.
            }
        }
    }


    //페이스북에서 로그인한 정보를 파이어베이스에 연동하느 함수이다.
    private fun firebaseAuthWithFacebook(result: LoginResult?) {
        var credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("facebook login")
                Toast.makeText(applicationContext, "로그인에 성공했습니다.", Toast.LENGTH_LONG).show()
                startActivity(ConfigurationActivity.getLaunchIntent(this))
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }
        }
    }

    //액티비티에서 결과값을 받았을 때 실행하는 함수이다.
    //로그인을 하고 나서 토큰을 받아 올 것이다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWhitGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }


    //페이스북 헤시키 생성 함수
    fun printHashKey(pContext: Context) {
        try {
            val info = pContext.packageManager
                .getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                println("printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
        } catch (e: Exception) {
        }
    }


    // 외부에서 로그아웃을 하게 해주는 함수이다.
    // 메인에서 호출하는 것을 볼 수 있다.
    companion object {
        fun getLaunchIntent(from: MainActivity) = Intent(from, loginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        fun getLaunchIntent(from: ConfigurationActivity) = Intent(from, loginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        fun getLaunchIntent(from: Context) = Intent(from, loginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }


    }
}