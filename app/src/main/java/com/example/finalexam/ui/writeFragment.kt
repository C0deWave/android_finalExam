package com.example.finalexam.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.finalexam.MainActivity
import com.example.finalexam.R
import com.example.finalexam.dataClass.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.fragment_write.*
import java.util.*

class writeFragment : Fragment() {

    val GET_GALLERY_IMAGE = 200;

    //선택된 이미지의 Uri
    var selectImageUri : Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_write, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*이미지 업로드 버튼 구현*/
        galleryButton.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            startActivityForResult(intent, GET_GALLERY_IMAGE)
        }


        /*게시글 올리기 버튼*/
        sendButton.setOnClickListener {
            if (contentEditText.text.isEmpty() || titleEditText.text.isEmpty()) {
                Toast.makeText(requireContext(), "제목과 내용을 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //Post객체생성
            val post = Post()
            //참조값을 할당합니다.
            val newRef = FirebaseDatabase.getInstance().getReference("Posts").push()
            //데이터를 할당합니다.
            post.writeTime = ServerValue.TIMESTAMP
            post.title = titleEditText.text.toString()
            post.message = contentEditText.text.toString()
            post.postId = newRef.key.toString()
            post.writerId = FirebaseAuth.getInstance().currentUser?.displayName!!
            post.descWriteTime = -1 * Date().time

            val storage = FirebaseStorage.getInstance().getReference("/image/${post.postId}")
            //나중에 이미지 업로드 부분 시간되면 구현하기
            val upload = selectImageUri?.let { it1 -> storage.putFile(it1) }

            upload?.addOnFailureListener{
                Log.d("image","이미지 업로드 실패")
            }?.addOnSuccessListener {
                Log.d("image","이미지 업로드 성공")
            }

            val urlTask = upload?.continueWithTask{ task ->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                storage.downloadUrl
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    post.bgUri = task.result.toString()

                    newRef.setValue(post)
                    Toast.makeText(requireContext(),"아마자 추가해서 저장 성공!!!",Toast.LENGTH_LONG).show()

                    (activity as MainActivity).setFragment(noticeBoardFragment.newInstance())
                }
            }


            if(urlTask == null) {
                Log.d("게시글 업로드", "게시글 업로")
                newRef.setValue(post)
                Toast.makeText(requireContext(), "이미지 없이 저장 성공!!!", Toast.LENGTH_LONG).show()

                (activity as MainActivity).setFragment(noticeBoardFragment.newInstance())
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.data != null){
            selectImageUri = data.data!!
            imageView6.setImageURI(selectImageUri)
        }
    }

    companion object {
        fun newInstance(): writeFragment {
            return writeFragment()
        }
    }
}