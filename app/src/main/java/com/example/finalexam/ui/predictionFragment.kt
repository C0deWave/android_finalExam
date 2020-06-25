package com.example.finalexam.ui


import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.anupkumarpanwar.scratchview.ScratchView
import com.example.finalexam.R
import kotlinx.android.synthetic.main.fragment_prediction.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class predictionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_prediction, container, false)
    }

    override fun onStart() {
        super.onStart()

        //volley로 합격률 api값 가져오기
        var result : String? = null

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(context)
        val url = "http://openapi.q-net.or.kr/api/service/rest/InquiryStatSVC/getGradPiPassList?serviceKey=5uS4tOJQn0fw3%2FjgZo8p9AY9kOz3VywROItpVQrcjFlHYo6OWPw5yJKuqvm4A%2BJ6mPHQjFhJxuZd%2BqkLBw4T0w%3D%3D&baseYY=2019&"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                result = response
                Toast.makeText(context,"Volley잘 작동함",Toast.LENGTH_LONG).show()
            },
            Response.ErrorListener { Log.d("Volley","Volley에러") })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
        //---------------------------------------------------------------------------------------------------------
        // 스크래치 기능의 리스너를 넣었습니다.
        scratchView.setRevealListener(object : ScratchView.IRevealListener{
            override fun onRevealed(scratchView: ScratchView?) {
                Toast.makeText(context,"축하드립니다.",Toast.LENGTH_LONG).show()
            }

            override fun onRevealPercentChangedListener(scratchView: ScratchView?, percent: Float) {

            }

        })
    }

    companion object {
        fun newInstance(): predictionFragment {
            return predictionFragment()
        }
    }
}
