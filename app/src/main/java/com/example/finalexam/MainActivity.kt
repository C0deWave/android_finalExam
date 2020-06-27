package com.example.finalexam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.example.finalexam.ui.accountConfigurationFragment
import com.example.finalexam.ui.calendarFragment
import com.example.finalexam.ui.noticeBoardFragment
import com.example.finalexam.ui.predictionFragment
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main2.*

//로그인이 완료된 후의 메인 클래스입니다.

class MainActivity : AppCompatActivity() {

    //로그인한 유저의 정보입니다.
    val user = FirebaseAuth.getInstance().currentUser

    //------------------------------------------------------------------------------------------
    //onCreate 함수입니다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)

        //처음에 하단에 app bar를 만들어 줍니다.
        meowBottomNavigation.add(MeowBottomNavigation.Model(1,R.drawable.ic_calendar))
        meowBottomNavigation.add(MeowBottomNavigation.Model(2,R.drawable.ic_stars))
        meowBottomNavigation.add(MeowBottomNavigation.Model(3,R.drawable.ic_bulltinboard))
        meowBottomNavigation.add(MeowBottomNavigation.Model(4,R.drawable.ic_about))

        //하단 네비게이션에 리스너를 붙여서 프레그먼트도 바뀌게 합니다.
        meowBottomNavigation.setOnClickMenuListener {
            when(it.id){
                1 -> {setFragment(calendarFragment.newInstance())}
                2 -> {setFragment(predictionFragment.newInstance())}
                3 -> {setFragment(noticeBoardFragment.newInstance())}
                4 -> {setFragment(accountConfigurationFragment.newInstance())}
            }
        }

        //처음 Fragment를 캘린더로 지정한다.
        setFragment(calendarFragment.newInstance())
        meowBottomNavigation.show(1)
    }//end of onCreate

    //-------------------------------------------------------------------------------------------
    //메뉴 전환시 Fragment를 전환하는 함수 입니다.
    fun setFragment(fragment : Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrame,fragment,"mainActivity")
            .commit()
    }//end of setFragment


    //-------------------------------------------------------------------------------------------
    // 세션 로그아웃 함수
    fun signOut() {
        lateinit var googleSignInClient: GoogleSignInClient

        //세션 로그아웃 구현
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //구글 클라이언트를 연결시킵니다.
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        FirebaseAuth.getInstance().signOut();

        // 구글 세션 로그 아웃
        googleSignInClient?.signOut()

        //페이스북 세션 로그아웃
        LoginManager.getInstance().logOut()

        //초기 화면으로 돌아간다.
        finish()
        startActivity(loginActivity.getLaunchIntent(this))
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }//end of signOut

    //-------------------------------------------------------------------------------------------
    //액티비티 스택을 쌓지 않고 화면을 넘어가게 해주는 싱글톤 함수입니다.
    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}