package com.example.finalexam.ui


import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.anupkumarpanwar.scratchview.ScratchView
import com.example.finalexam.R
import com.example.finalexam.predictXmlData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_prediction.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.time.LocalDateTime
import kotlin.random.Random

class predictionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prediction, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        //volley로 합격률 api값 가져오기
        var result : String? = null

        //api의 값
        var predictXml : ArrayList<predictXmlData>? = null

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        val url = "http://openapi.q-net.or.kr/api/service/rest/InquiryStatSVC/getGradPiPassList?serviceKey=5uS4tOJQn0fw3%2FjgZo8p9AY9kOz3VywROItpVQrcjFlHYo6OWPw5yJKuqvm4A%2BJ6mPHQjFhJxuZd%2BqkLBw4T0w%3D%3D&baseYY=2019&"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                result = String(response.toByteArray(charset("euc-kr")), charset("euc-kr"))
//                Toast.makeText(context,"Volley잘 작동함",Toast.LENGTH_LONG).show()
                //----------------------------------------------------------------
                //xml 파싱
                val pullParserFactory : XmlPullParserFactory
                try {
                    pullParserFactory = XmlPullParserFactory.newInstance()
                    val parser = pullParserFactory.newPullParser()
                    val inputStream = ByteArrayInputStream(result?.toByteArray(charset("euc-kr")))
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false)
                    parser.setInput(inputStream,null)

                    predictXml = parseXml(parser)

                    //---------------------------------------------------------------
                    //랜덤 함수를 해서 합격 예측을 합니다.
                    var predictResult = Random(LocalDateTime.now().hashCode()).nextInt(100)+1
                    Log.d("time","${LocalDateTime.now()}")
                    var cert1 = ""

                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        FirebaseDatabase.getInstance().reference
                            .child("users")
                            .child(it)
                            .addListenerForSingleValueEvent(object  : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    var data = p0.value as Map<String,Any>
                                    cert1 = data["cert1"].toString()

                                    when(cert1){
                                        "기능사" -> { setPredict(predictResult , predictXml?.get(5)?.starisyy1) }
                                        "기사" -> {setPredict(predictResult , predictXml?.get(2)?.starisyy1)}
                                        "기능장" -> {setPredict(predictResult , predictXml?.get(1)?.starisyy1)}
                                        "기술사" -> {setPredict(predictResult , predictXml?.get(0)?.starisyy1)}
                                        else -> {Toast.makeText(context,"cert1의 값이 이상합니다. ${cert1}",Toast.LENGTH_LONG).show()}
                                    }
                                }
                            }).toString()
                    }



                }catch (e : XmlPullParserException){
                    e.printStackTrace()
                }catch (e : IOException){
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { Log.d("Volley","Volley에러") })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

        //---------------------------------------------------------------------------------------------------------
        // 스크래치 기능의 리스너를 넣었습니다.
        scratchView.setRevealListener(object : ScratchView.IRevealListener{
            override fun onRevealed(scratchView: ScratchView?) {
                AnimatorInflater.loadAnimator(context,R.animator.predicr_clear_animator).apply {
                    setTarget(scratchView)
                    start()
                }
            }

            override fun onRevealPercentChangedListener(scratchView: ScratchView?, percent: Float) {
                Log.d("percent","${percent}")

                //핑거를 사라지게 합니다.
                fingerImage.clearAnimation()
                fingerImage.visibility = View.INVISIBLE

                if (percent > 0.1){
                    this.onRevealed(scratchView)

                    //리스너를 해제합니다.
                    scratchView?.setRevealListener(object :ScratchView.IRevealListener{
                        override fun onRevealed(scratchView: ScratchView?) {}
                        override fun onRevealPercentChangedListener(
                            scratchView: ScratchView?,
                            percent: Float
                        ) {}
                    })
                }
            }

        })

    }//end of onStart

    fun setPredict(predictResult: Int, starisyy1: String?) {
        //합격 확률 안쪽으로 들어온 경우
        Toast.makeText(context,"${predictResult} ${starisyy1}",Toast.LENGTH_LONG).show()
        if (predictResult < starisyy1!!.toInt()){
            predictionResultImageView.setBackgroundColor( requireContext().getColor(R.color.goodnews) )
            predictionResultImageView.setImageResource(R.drawable.crystallballgood)
        }else{
            predictionResultImageView.setBackgroundColor( requireContext().getColor(R.color.badnews) )
            predictionResultImageView.setImageResource(R.drawable.crystallballbad)
        }

        //준비가 끝나면 예언준비 이미지를 페이드 아웃으로 사라지게 합니다.
        AnimatorInflater.loadAnimator(context,R.animator.predicr_clear_animator).apply {
            addListener(object : AnimatorListenerAdapter(){
                //애니매이션이 끝나면 이미지를 안보이게 해서 수정이 클릭되게 합니다.
                override fun onAnimationEnd(animation: Animator?) {
                    imageView2.visibility =View.INVISIBLE
                }
            })
            setTarget(imageView2)
            start()
        }

        fingerImage.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(activity,R.anim.finger_animation)
        fingerImage.startAnimation(animation)
    }

    @Throws (XmlPullParserException::class, IOException::class)
    fun parseXml(parser: XmlPullParser?): ArrayList<predictXmlData>? {
        var dataArray : ArrayList<predictXmlData>? = null
        var eventType = parser?.eventType
        var data : predictXmlData? = null

        while (eventType != XmlPullParser.END_DOCUMENT){
            val tagName : String
            when(eventType){
                XmlPullParser.START_DOCUMENT->dataArray = ArrayList()
                XmlPullParser.START_TAG -> {
                    tagName = parser!!.name
                    if (tagName == "item"){
                        data = predictXmlData()
                    }else if (data != null){
                        if (tagName == "statisyy1"){
                            data.starisyy1 = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    tagName = parser!!.name
                    if (tagName.equals("item", ignoreCase = true) && data != null){
                        dataArray!!.add(data)
                    }
                }
            }
            eventType = parser!!.next()
        }
        return dataArray
    }

    companion object {
        fun newInstance(): predictionFragment {
            return predictionFragment()
        }
    }
}
