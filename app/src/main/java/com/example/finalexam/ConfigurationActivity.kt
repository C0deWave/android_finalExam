package com.example.finalexam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.net.HttpURLConnection
import java.net.*

class ConfigurationActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //액션바 숨기
        supportActionBar?.hide()

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

        var thread = NetworkThread()
        thread.start()
    }

    inner class NetworkThread() : Thread(){
        override fun run() {


            Log.d("Thread","스레드 시작")

            var site = URL( "http://openapi.q-net.or.kr/api/service/rest/InquiryStatSVC/getGradPiPassList?serviceKey=QR6Eti6A0zHnybQlRIidIklUWdIf9bl5bt3coyBro2ldfN%2FsxvuIwJ8O4HNQAhBV%2Fia8yktKc1xmVa29qQaDMA%3D%3D&baseYY=2019&")
            var conn : HttpURLConnection = site.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            var input = conn.inputStream
            var isr = InputStreamReader(input)
            var br = BufferedReader(isr)

            var str : String? = null
            var buf = StringBuffer()
            do {
                str = br.readLine()
                println(str)

                if (str != null){
                    Log.d("Thread","${str}")
                    buf.append(str)
                }
            }while (str != null)

            runOnUiThread {
                textView5.setText(str)
            }
            Log.d("Thread","스레드 종료")
            println(buf.toString())
        }

        fun parse(inputStream: InputStream): List<*> {
            inputStream.use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(inputStream, null)
                parser.nextTag()
                return readFeed(parser)
            }
        }


        // We don't use namespaces
        val ns: String? = null

        @Throws(XmlPullParserException::class, IOException::class)
        fun readFeed(parser: XmlPullParser): List<Entry> {
            val entries = mutableListOf<Entry>()

            parser.require(XmlPullParser.START_TAG, ns, "response")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                // Starts by looking for the entry tag
                if (parser.name == "items") {
                    entries.add(readEntry(parser))
                } else {
                    skip(parser)
                }
            }
            return entries
        }

        // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
        // to their respective "read" methods for processing. Otherwise, skips the tag.
        @Throws(XmlPullParserException::class, IOException::class)
        private fun readEntry(parser: XmlPullParser): Entry {
            parser.require(XmlPullParser.START_TAG, ns, "body")
            var title: String? = null
            var summary: String? = null
            var link: String? = null
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                when (parser.name) {
                    "items" -> title = readTitle(parser)
                    "summary" -> summary = readSummary(parser)
                    "link" -> link = readLink(parser)
                    else -> skip(parser)
                }
            }
            return Entry(title, summary, link)
        }

        // Processes title tags in the feed.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readTitle(parser: XmlPullParser): String {
            parser.require(XmlPullParser.START_TAG, ns, "title")
            val title = readText(parser)
            parser.require(XmlPullParser.END_TAG, ns, "title")
            return title
        }

        // Processes link tags in the feed.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readLink(parser: XmlPullParser): String {
            var link = ""
            parser.require(XmlPullParser.START_TAG, ns, "link")
            val tag = parser.name
            val relType = parser.getAttributeValue(null, "rel")
            if (tag == "link") {
                if (relType == "alternate") {
                    link = parser.getAttributeValue(null, "href")
                    parser.nextTag()
                }
            }
            parser.require(XmlPullParser.END_TAG, ns, "link")
            return link
        }

        // Processes summary tags in the feed.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readSummary(parser: XmlPullParser): String {
            parser.require(XmlPullParser.START_TAG, ns, "summary")
            val summary = readText(parser)
            parser.require(XmlPullParser.END_TAG, ns, "summary")
            return summary
        }

        // For the tags title and summary, extracts their text values.
        @Throws(IOException::class, XmlPullParserException::class)
        private fun readText(parser: XmlPullParser): String {
            var result = ""
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.text
                parser.nextTag()
            }
            return result
        }

        @Throws(XmlPullParserException::class, IOException::class)
        private fun skip(parser: XmlPullParser) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                throw IllegalStateException()
            }
            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    XmlPullParser.END_TAG -> depth--
                    XmlPullParser.START_TAG -> depth++
                }
            }
        }
    }
    //-------------네트워크 스레드 종료

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

data class Entry(val title: String?, val summary: String?, val link: String?)
